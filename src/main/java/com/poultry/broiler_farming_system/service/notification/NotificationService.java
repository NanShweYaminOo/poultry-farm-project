package com.poultry.broiler_farming_system.service.notification;

import com.poultry.broiler_farming_system.entity.User;

// Generic in-app/push/email style notification to a user. No channel is
// mandated by the spec for this one (unlike SmsService, which is explicitly
// required for the 6pm daily-log enforcement warning) -- swap the
// implementation for a real push/email provider when one exists.
public interface NotificationService {

    void notify(User recipient, String message);
}
