package org.edbb.data;

/**
 * Database access layer
 */
public interface DataStore {

    /**
     * Load a given event from the data store
     * @param id
     * @return
     */
    BBEvent getEvent(long id);

    /**
     * Add a new event to the data store returning it's unique ID
     * @param event
     * @return
     */
    long addEvent(BBEvent event);


}
