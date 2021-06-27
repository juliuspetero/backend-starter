package com.mojagap.backenstarter.infrastructure.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.logging.Level;
import java.util.logging.Logger;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class InternalServerErrorException extends RuntimeException {

    private static final Logger LOG = Logger.getLogger(InternalServerErrorException.class.getName());

    public InternalServerErrorException(String message) {
        super(message);
        LOG.log(Level.WARNING, message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
