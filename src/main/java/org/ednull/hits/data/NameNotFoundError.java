package org.ednull.hits.data;

/**
 * Thrown when the name requested has no known ID yet
 */
public class NameNotFoundError extends Exception {
    public NameNotFoundError(String name) {
        this.name = name;
    }

    public String name;
}
