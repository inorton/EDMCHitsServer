package org.ednull.hits.server;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Throw when a place does not exist
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "This location is unknown")
public class NoSuchLocationError extends RuntimeException {
    @Override
    public synchronized Throwable fillInStackTrace() {
        return null;
    }
}
