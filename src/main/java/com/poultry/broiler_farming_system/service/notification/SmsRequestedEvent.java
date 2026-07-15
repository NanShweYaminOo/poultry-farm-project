package com.poultry.broiler_farming_system.service.notification;

/**
 * Published instead of calling SmsService directly, so the actual gateway
 * call can be deferred to after the publishing transaction commits and run
 * off-thread -- see SmsRequestedEventListener. Carries plain values (not
 * the User/Batch entities) on purpose: by the time the async listener runs,
 * the original transaction/persistence context is long closed, so a
 * detached entity reference would risk a LazyInitializationException the
 * moment anything but an already-loaded field is touched.
 */
public record SmsRequestedEvent(String phoneNumber, String message) {
}
