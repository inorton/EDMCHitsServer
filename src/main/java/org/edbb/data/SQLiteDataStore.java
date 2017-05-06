package org.edbb.data;

import javax.swing.plaf.nimbus.State;
import java.sql.*;

/**
 * Created by inb on 06/05/2017.
 */
public class SQLiteDataStore implements DataStore {

    Connection connection;

    void createTables() throws SQLException {
        try (Statement sth = connection.createStatement()) {
            String events = "CREATE TABLE IF NOT EXISTS bbEvents ( " +
                    " id INTEGER PRIMARY KEY, " +
                    " starSystem TEXT NOT NULL, " +
                    " eventName TEXT NOT NULL, " +
                    " timeStamp INTEGER, " +
                    " shipType TEXT NOT NULL, " +
                    " hostilePilot TEXT NOT NULL, " +
                    " hostileNpc BOOLEAN NOT NULL )";
            sth.execute(events);
        }
    }

    public SQLiteDataStore(String url) throws SQLException {
        connection = DriverManager.getConnection(url);
        createTables();
    }

    @Override
    public BBEvent getEvent(long id) {
        BBEvent event = new BBEvent();
        try {
            try (PreparedStatement sth = connection.prepareStatement("SELECT " +
                        "starSystem, eventName, timeStamp, shipType, " +
                        "hostilePilot, hostileNpc FROM bbEvents " +
                        "WHERE id = ?")) {
                sth.clearParameters();
                sth.setLong(1, id);
                ResultSet resultSet = sth.executeQuery();
                if (resultSet.first()) {
                    event.setStarSystem(resultSet.getString("starSystem"));
                    event.setEventId(id);
                    event.setEventName(resultSet.getString("eventName"));
                    event.setHostileParty(resultSet.getString("hostilePiloy"));
                    event.setNpc(resultSet.getBoolean("hostileNpc"));
                    event.setShipType(resultSet.getString("shipType"));
                    event.setTimestamp(resultSet.getTimestamp("timeStamp"));
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
        return 0;
    }
}
