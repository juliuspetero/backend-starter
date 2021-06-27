package com.mojagap.backenstarter.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.logging.Level;
import java.util.logging.Logger;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException {
    private static final Logger LOG = Logger.getLogger(UnauthorizedException.class.getName());

    public UnauthorizedException(String message) {
        super(message);
        LOG.log(Level.WARNING, message, HttpStatus.UNAUTHORIZED);
    }
}
