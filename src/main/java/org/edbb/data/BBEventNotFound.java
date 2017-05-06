package org.edbb.data;

/**
 * Thrown when there is no event.
 */
public class BBEventNotFound extends RuntimeException {
    public BBEventNotFound(String message) {
        super(message);
    }
}
