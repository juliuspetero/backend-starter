package com.mojagap.backenstarter.infrastructure.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExceptionResponse {
    private String message;
    private Integer statusCode;
}
