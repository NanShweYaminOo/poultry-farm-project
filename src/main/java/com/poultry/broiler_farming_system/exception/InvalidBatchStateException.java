package com.poultry.broiler_farming_system.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidBatchStateException extends RuntimeException {

    public InvalidBatchStateException(String message) {
        super(message);
    }
}
