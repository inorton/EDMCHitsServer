package org.ednull.hits.data;

import java.sql.Timestamp;

/**
 * Someone has spotted a criminal
 */
public class BBSighting extends BBSigned {
    public long starSystem;
    public long pilot;
    public Timestamp timestamp;
}
