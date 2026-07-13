package com.poultry.broiler_farming_system.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

// Placeholder: logs instead of actually sending an SMS. No SMS gateway
// (Twilio, etc) is wired up or credentialed -- replace this bean with a
// real implementation of SmsService when one exists; nothing else in the
// codebase needs to change.
@Service
public class LoggingSmsService implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(LoggingSmsService.class);

    @Override
    public void sendSms(String phoneNumber, String message) {
        log.info("[SMS] to {}: {}", phoneNumber, message);
    }
}
