package com.poultry.broiler_farming_system.service.notification;

// Explicitly required by spec for the 6pm daily-log enforcement job: an SMS
// to the farmer's stored phone_number, distinct from the generic
// NotificationService channel.
public interface SmsService {

    void sendSms(String phoneNumber, String message);
}
