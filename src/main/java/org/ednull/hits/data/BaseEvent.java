package org.ednull.hits.data;

import java.time.Instant;

public abstract class BaseEvent {
    public String identity;
    public long systemId;
    public Instant time;
}
