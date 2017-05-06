package org.edbb.data;

import java.sql.Timestamp;

/**
 * Represent an individual event
 */
public class BBEvent {

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
     * The type of ship this event happend to
     * @return
     */
    public String getShipType() {
        return shipType;
    }

    public void setShipType(String shipType) {
        this.shipType = shipType;
    }

    /**
     * The identifier of the app that submitted this event
     * @return
     */
    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
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
    public String getStarSystem() {
        return starSystem;
    }

    public void setStarSystem(String starSystem) {
        this.starSystem = starSystem;
    }

    /**
     * The in-game name of the hostile pilot that initiated this event.
     * @return
     */
    public String getHostileParty() {
        return hostileParty;
    }

    public void setHostileParty(String hostileParty) {
        this.hostileParty = hostileParty;
    }

    /**
     * False if the hostile pilot was a member of the Elite Pilot's Federation.
     * @return
     */
    public Boolean getNpc() {
        return Npc;
    }

    public void setNpc(Boolean npc) {
        Npc = npc;
    }

    long eventId;
    Timestamp timestamp;
    String shipType;
    String identity;
    String eventName;
    String starSystem;
    String hostileParty;
    Boolean Npc;
}
