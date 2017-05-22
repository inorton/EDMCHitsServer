package org.ednull.hits.data;

import java.time.Instant;

/**
 * Represent the basic journal event data for IncidentScanner
 */

class JournalEvent extends BaseEvent {

    public String eventType;

    // true if submitted by edmc - edmc does not submit commodity/shipyard/outfitting events as captain
    public boolean asCaptain;
}
