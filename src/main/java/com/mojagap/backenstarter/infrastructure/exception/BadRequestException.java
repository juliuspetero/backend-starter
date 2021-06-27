package com.mojagap.backenstarter.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.logging.Level;
import java.util.logging.Logger;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    private static final Logger LOG = Logger.getLogger(BadRequestException.class.getName());

    public BadRequestException(String reason) {
        super(reason);
        LOG.log(Level.WARNING, reason, HttpStatus.BAD_REQUEST);
    }
}
