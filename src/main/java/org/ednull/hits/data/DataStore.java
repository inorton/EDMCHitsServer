package org.ednull.hits.data;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

/**
 * Database access layer
 */
public interface DataStore {

    /**
     * Report a crime
     * @param crime
     */
    void addCrime(BBCrime crime);

    /**
     * Report contact
     * @param criminal
     * @param starSystem
     * @param  timestamp
     */
    void addSighting(long criminal, long starSystem, Timestamp timestamp);

    /**
     * get the ID for a given app name
     * @param appname
     * @return
     */
    long lookupApp(String appname);

    /**
     * get the ID for a given system name
     * @param sysName
     * @return
     */
    long lookupSystem(String sysName) throws NameNotFoundError;

    /**
     * Add a new system to the database
     * @param sysName
     * @param x
     * @param y
     * @param z
     * @return
     */
    long addSystem(String sysName, double x, double y, double z);

    /**
     * Find the name of a system
     * @param id
     * @return
     * @throws NameNotFoundError
     */
    String lookupSystem(long id) throws NameNotFoundError;

    /**
     * Find systems matching the given substring
     * @param substring
     * @return
     */
    List<String> searchSystems(String substring) throws SQLException;

    /**
     * Add a new event to the data store returning it's unique ID
     * @param event
     * @return
     */
    long addEvent(BBEvent event);

    /**
     * Get the top few most busy systems
     * @param max
     * @param hours only include events after many hours in the past
     * @return
     */
    List<SystemReport> busySystems(int max, int hours);

    /**
     * Get the top few most dangerous systems
     * @param max
     * @param hours only include events newer than this
     * @return
     */
    List<SystemReport> dangerSystems(int max, int hours);

    /**
     * Delete excess database events
     * @param maxEventCount
     */
    void prune(long maxEventCount);

    /**
     * Produce a report about a system
     * @param systemId
     * @param hours
     * @return
     */
    SystemReport getReport(long systemId, int hours) throws NameNotFoundError;

    /**
     * Given a name, get a pilot id
     * @param name
     * @return
     */
    long lookupPilot(String name);

    /**
     * Lookup the criminal record of a pilot
     * @param pilot
     * @return
     */
    CriminalRecord lookupCrimes(long pilot, long hours);
}
