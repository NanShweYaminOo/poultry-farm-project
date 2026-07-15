package com.poultry.broiler_farming_system.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

// Default everywhere except the "sms-live" profile (see VonageSmsService):
// dev, and every test, get this stub with no active profile or credentials
// required. Logs instead of actually sending an SMS.
@Service
@Profile("!sms-live")
public class LoggingSmsService implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(LoggingSmsService.class);

    @Override
    public void sendSms(String phoneNumber, String message) {
        log.info("[SMS] to {}: {}", phoneNumber, message);
    }
}
