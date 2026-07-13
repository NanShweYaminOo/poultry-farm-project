package com.poultry.broiler_farming_system.service.notification;

import com.poultry.broiler_farming_system.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

// Placeholder: logs instead of actually pushing/emailing. There is no
// notifications table or push/email provider wired up yet -- replace this
// bean with a real implementation of NotificationService when one exists;
// nothing else in the codebase needs to change.
@Service
public class LoggingNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationService.class);

    @Override
    public void notify(User recipient, String message) {
        log.info("[NOTIFY] to {} <{}>: {}", recipient.getUsername(), recipient.getEmail(), message);
    }
}
