package com.poultry.broiler_farming_system.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Thrown when an Admin has not yet set a required key in
// system_configurations (a medicine dosage/price formula, a duration
// constant, etc).
@ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
public class MissingSystemConfigurationException extends RuntimeException {

    public MissingSystemConfigurationException(String message) {
        super(message);
    }
}
