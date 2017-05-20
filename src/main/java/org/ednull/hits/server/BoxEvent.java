package org.ednull.hits.server;

/**
 * event's can be "Interdicted", "Died", "EscapeInterdiction".
 */
public class BoxEvent {
    public String app;  // identifier of the program name/version that submitted this
    public String submitter;  // unique string (eg, sha1 hash) of the submitting commander
    public String eventName;
    public String starSystem;  // system name
    public long timestamp; // epoch secs
}
