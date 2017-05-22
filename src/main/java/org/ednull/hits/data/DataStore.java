package org.ednull.hits.data;

import java.sql.Timestamp;
import java.util.List;

/**
 * Database access layer
 */
public interface DataStore {

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
     * Load a given event from the data store
     * @param id
     * @return
     */
    BBEvent getEvent(long id) throws BBEventNotFound;

    /**
     * Add a new event to the data store returning it's unique ID
     * @param event
     * @return
     */
    long addEvent(BBEvent event);

    /**
     * Get the top few most busy systems
     * @param max
     * @param since only include events after this time
     * @return
     */
    String[] busySystems(int max, long since);

    /**
     * Get the top few most dangerous systems
     * @param max
     * @param since only include events after this time
     * @return
     */
    String[] dangerSystems(int max, long since);
}
