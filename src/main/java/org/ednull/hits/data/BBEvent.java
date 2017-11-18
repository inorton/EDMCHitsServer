package org.ednull.hits.data;

import java.sql.Timestamp;

/**
 * Represent an individual event from the database
 */
public class BBEvent {

    public static final String DESTORYED = "Destroyed";
    public static final String ARRIVED = "Arrived";
    public static final String INTERDICTED = "Interdicted";

    // landed at a base,
    public static final String DOCKED = "Docked";

    // just entered a system
    public static final String JUMPEDIN = "JumpedIn";

    /**
     * Unique identifier for this event log.
     * @return
     */
    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    /**
     * When this event occurred (game time)
     * @return
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * The identifier of the commander that submitted this event
     * @return
     */
    public String getSubmitter() {
        return submitter;
    }

    public void setSubmitter(String submitter) {
        this.submitter = submitter;
    }

    /**
     * The event name as defined in the journal.
     * @see <a href="http://edcodex.info/?m=doc">ED Codex Journal Docs</a>
     * @return
     */
    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    /**
     * The name of the star system this event occured at.
     * @return
     */
    public long getStarSystem() {
        return starSystem;
    }

    public void setStarSystem(long starSystem) {
        this.starSystem = starSystem;
    }


    /**
     * The app that submitted this message
     * @return
     */
    public long getApp() {
        return app;
    }

    public void setApp(long app) {
        this.app = app;
    }

    private long eventId;
    private Timestamp timestamp;
    private long app;
    private long starSystem;

    private String submitter;
    private String eventName;
}
