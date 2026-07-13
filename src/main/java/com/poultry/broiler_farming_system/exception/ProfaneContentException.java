package com.poultry.broiler_farming_system.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ProfaneContentException extends RuntimeException {

    public ProfaneContentException(String message) {
        super(message);
    }
}
