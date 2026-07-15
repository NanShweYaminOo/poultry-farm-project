package com.poultry.broiler_farming_system.exception;

// Never surfaced to an HTTP response -- SMS sends only ever happen from
// SmsRequestedEventListener, async and after commit, long after any
// request that could have returned this to a client has completed. No
// @ResponseStatus for that reason; GlobalExceptionHandler never sees it.
public class SmsDeliveryException extends RuntimeException {

    public SmsDeliveryException(String message) {
        super(message);
    }

    public SmsDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
