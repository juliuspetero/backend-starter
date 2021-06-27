package com.mojagap.backenstarter.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.logging.Level;
import java.util.logging.Logger;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {
    private static final Logger LOG = Logger.getLogger(ForbiddenException.class.getName());

    public ForbiddenException(String message) {
        super(message);
        LOG.log(Level.WARNING, message, HttpStatus.FORBIDDEN);
    }
}
