package org.ednull.hits.data;

import org.sqlite.SQLiteConnection;

import java.sql.*;
import java.util.ArrayList;

/**
 * Created by inb on 06/05/2017.
 */
public class SQLiteDataStore implements DataStore {

    Connection connection;

    private final String connectionUrl;

    public final static String EVENTS_TABLE = "events";
    public final static String PILOTS_TABLE = "pilots";

    public final static String APPS_TABLE = "apps";
    public final static String SYSTEMS_TABLE = "systems";


    private void createTables() throws SQLException {
        try (Statement sth = connection.createStatement()) {
            String events = new StringBuilder()
                    .append(String.format("CREATE TABLE IF NOT EXISTS %S ( ", EVENTS_TABLE))
                    .append(" id INTEGER PRIMARY KEY, ")
                    .append(" app INTEGER NOT NULL, ")
                    .append(" submitter TEXT, ")
                    .append(" starSystem INTEGER NOT NULL, ")
                    .append(" eventName TEXT NOT NULL, ")
                    .append(" timeStamp INTEGER NOT NULL )").toString();
            sth.execute(events);

            createIndex(sth, EVENTS_TABLE, "events_system",
                    new String[]{"starSystem"}, false);
            createIndex(sth, EVENTS_TABLE, "events_event",
                    new String[]{"eventName"}, false);
            createIndex(sth, EVENTS_TABLE, "events_app",
                    new String[]{"app"}, false);
            createIndex(sth, EVENTS_TABLE, "events_submitter",
                    new String[]{"submitter"}, false);
            createIndex(sth, EVENTS_TABLE, "events_location",
                    new String[]{"eventName", "starSystem"}, false);

            createNameTable(sth, PILOTS_TABLE, true);
            createNameTable(sth, APPS_TABLE, true);

            String syscoords = new StringBuilder()
                    .append(String.format("CREATE TABLE IF NOT EXISTS %S (", SYSTEMS_TABLE))
                    .append(" id INTEGER PRIMARY KEY, ")
                    .append(" name TEXT, ")
                    .append(" x REAL, ")
                    .append(" y REAL, ")
                    .append(" z REAL )").toString();
            sth.execute(syscoords);
            createIndex(sth, SYSTEMS_TABLE, "systems_name",
                    new String[]{"name"}, true);
        }
    }

    private void createIndex(Statement sth, String tableName, String indexName, String[] columns, boolean unique) throws SQLException {
        String uniqueIndex = unique ? "UNIQUE" : "";
        String index = new StringBuilder()
                .append(String.format("CREATE %S INDEX IF NOT EXISTS index_%S ", uniqueIndex, indexName))
                .append(String.format(" ON %S ", tableName))
                .append(String.format("(%S)", String.join(", ", columns))).toString();
        sth.execute(index);
    }


    private void createNameTable(Statement sth, String tableName, boolean idPrimaryKey) throws SQLException {
        String primaryKey = idPrimaryKey ? "PRIMARY KEY" : "";

        String table = new StringBuilder()
                .append(String.format("CREATE TABLE IF NOT EXISTS %S ( ", tableName))
                .append(String.format(" id INTEGER %S, ", primaryKey))
                .append(" name TEXT NOT NULL )").toString();
        sth.execute(table);

        if (!idPrimaryKey) {
            createIndex(sth, tableName, "name_" + tableName, new String[] { "id", "name"}, true);
        }
    }

    public SQLiteDataStore(String url) throws SQLException {
        connection = DriverManager.getConnection(url);
        connectionUrl = url;
        createTables();
    }

    /**
     * Get a connection set in transaction mode.
     * @return
     * @throws SQLException
     */
    private Connection getWriteConnection() throws SQLException {
        Connection c = DriverManager.getConnection(connectionUrl);
        c.setAutoCommit(false);
        return c;
    }

    public String getSystemName(long id) {
        return lookupName(id, SYSTEMS_TABLE);
    }

    public String getPilotName(long id) {
        return lookupName(id, PILOTS_TABLE);
    }

    public String getAppName(long id) {
        return lookupName(id, APPS_TABLE);
    }

    private String lookupName(long id, String table) {
        String name = null;
        try (PreparedStatement sth = connection.prepareStatement(new StringBuilder()
                .append(String.format("SELECT name FROM %s ", table))
                .append("WHERE id = ?").toString())) {
            sth.clearParameters();
            sth.setLong(1, id);
            ResultSet resultSet = sth.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("name");
            }
        } catch (SQLException err ){
            err.printStackTrace();
            throw new RuntimeException(err.getMessage());
        }
        return name;
    }

    private long lookupId(String table, String name) {
        return lookupId(connection, table, name);
    }

    private long lookupId(Connection conn, String table, String name) {
        long id = 0;
        StringBuilder sb = new StringBuilder()
                .append(String.format("SELECT id FROM %S ", table))
                .append("WHERE name == ?");
        String query = sb.toString();

        try (PreparedStatement sth = conn.prepareStatement(query)) {
            sth.clearParameters();
            sth.setString(1, name);
            ResultSet resultSet = sth.executeQuery();

            if (resultSet.next()) {
                return resultSet.getLong("id");
            }
        } catch (SQLException err ){
            err.printStackTrace();
        }
        return id;
    }

    private long getNameId(String table, String name) throws SQLException {
        long exists = lookupId(table, name);
        if (exists > 0) {
            return exists;
        }
        // save the new name,
        return saveName(table, name);
    }

    private synchronized long saveName(String table, String name) throws SQLException {
        try (Connection writer = getWriteConnection()) {
            PreparedStatement sth = writer.prepareStatement(new StringBuilder()
                    .append(String.format("INSERT INTO %S ", table))
                    .append("(name) ")
                    .append("VALUES ( ? ) ")
                    .toString());
            sth.setString(1, name);
            sth.execute();
            writer.commit();

            // could use last "SELECT last_insert_rowid()" but this is simple
            return lookupId(writer, table, name);
        }
    }


    private PreparedStatement getSelectEvent() throws SQLException {
        PreparedStatement sth = connection.prepareStatement(new StringBuilder()
                .append("SELECT ").append("starSystem, eventName, timeStamp, app, submitter, wing")
                .append(String.format("hostileParty FROM %s ", EVENTS_TABLE))
                .append("WHERE id = ?").toString());
        sth.clearParameters();
        return sth;
    }

    @Override
    public BBEvent getEvent(long id) {
        BBEvent event = new BBEvent();
        try {
            try (PreparedStatement sth = getSelectEvent() ) {
                sth.setLong(1, id);
                ResultSet resultSet = sth.executeQuery();

                if (resultSet.first()) {
                    event.setEventId(id);
                    event.setStarSystem(resultSet.getInt("starSystem"));
                    event.setSubmitter(resultSet.getString("submitter"));
                    event.setEventName(resultSet.getString("eventName"));
                    event.setTimestamp(resultSet.getTimestamp("timeStamp"));
                    event.setApp(resultSet.getLong("app"));
                    return event;
                }
            }

        } catch (SQLException err ){
            err.printStackTrace();
            throw new RuntimeException(err.getMessage());
        }

        throw new BBEventNotFound("could not find event: " + id);
    }

    @Override
    public long addEvent(BBEvent event) {
        // check for wing, app and system
        try (Connection writer = getWriteConnection()) {
            PreparedStatement sth = writer.prepareStatement(new StringBuilder()
                    .append(String.format("INSERT INTO %S ", EVENTS_TABLE))
                    .append("app, submitter, starSystem, eventName, timeStamp ")
                    .append("VALUES (?, ?, ?, ?, ?")
                    .toString());

            sth.setLong(1, event.getApp());
            sth.setString(2, event.getSubmitter());
            sth.setLong(3, event.getStarSystem());
            sth.setString(4, event.getEventName());
            sth.setTimestamp(5, event.getTimestamp());

            sth.execute();
            writer.commit();
            Statement idx = writer.createStatement();
            ResultSet rs = idx.executeQuery(
                    String.format("SELECT last_insert_id() as last_id from %S", EVENTS_TABLE));
            return rs.getLong("last_id");

        } catch (SQLException err) {
            throw new RuntimeException("could not add event:" + err);
        }
    }

    @Override
    public long lookupApp(String appname) {
        try {
            return getNameId(APPS_TABLE, appname);
        } catch (SQLException err) {
            throw new RuntimeException("could not lookup/add app:" + err);
        }
    }

    @Override
    public long lookupSystem(String sysName) throws NameNotFoundError {
        long exists = lookupId(SYSTEMS_TABLE, sysName);
        if (exists > 0) {
            return exists;
        }
        throw new NameNotFoundError(sysName);
    }

    @Override
    public String lookupSystem(long id) throws NameNotFoundError {
        String name = lookupName(id, SYSTEMS_TABLE);
        if (name == null)
            throw new NameNotFoundError(String.format("id=%d", id));
        return name;
    }

    @Override
    public long addSystem(String name, double x, double y, double z) {
        try (Connection writer = getWriteConnection()) {
            long exists = lookupId(writer, SYSTEMS_TABLE, name);
            if (exists == 0) {
                PreparedStatement sth = writer.prepareStatement(new StringBuilder()
                        .append(String.format("INSERT INTO %S ", SYSTEMS_TABLE))
                        .append("(name, x , y, z) ")
                        .append("VALUES ( ?, ?, ?, ? ) ")
                        .toString());
                sth.setString(1, name);
                sth.setDouble(2, x);
                sth.setDouble(3, y);
                sth.setDouble(4, z);
                sth.execute();
                writer.commit();

                // could use last "SELECT last_insert_rowid()" but this is simple
                return lookupId(writer, SYSTEMS_TABLE, name);
            } else {
                return exists;
            }

        } catch (SQLException err) {
            throw new RuntimeException("could not add system:" + err);
        }
    }

    @Override
    public String[] busySystems(int max, long since) {
        return new String[0];
    }

    @Override
    public String[] dangerSystems(int max, long since) {
        return new String[0];
    }
}