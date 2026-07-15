package com.poultry.broiler_farming_system.service.notification;

import com.poultry.broiler_farming_system.dto.notification.NotificationResponse;
import com.poultry.broiler_farming_system.entity.AccountUpgradeRequest;
import com.poultry.broiler_farming_system.entity.Batch;
import com.poultry.broiler_farming_system.entity.BatchAlarm;
import com.poultry.broiler_farming_system.entity.FeedbackTicket;
import com.poultry.broiler_farming_system.entity.PaymentTransaction;
import com.poultry.broiler_farming_system.entity.User;
import com.poultry.broiler_farming_system.entity.UserWarning;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/**
 * Persisted, EN+MY localized in-app notifications. Every notify* method
 * writes a row synchronously, in the caller's own ambient transaction --
 * same rationale as SystemLogService.record(): a notification should only
 * exist if the action it describes actually committed, and a single local
 * DB insert is cheap enough to not need decoupling.
 *
 * notifyDailyLogMissing() is the one exception: it's the sole trigger the
 * original spec requires an SMS for. It still persists its row the same
 * synchronous way, but additionally publishes a SmsRequestedEvent so the
 * actual SMS gateway call happens off-thread, after commit -- see
 * SmsRequestedEventListener for why.
 */
public interface NotificationService {

    void notifyPaymentApproved(PaymentTransaction transaction);

    void notifyPaymentRejected(PaymentTransaction transaction);

    void notifyBatchAutoCompleted(Batch batch);

    void notifyBatchCycleEndingSoon(Batch batch, LocalDate cycleEndDate, int daysAhead);

    void notifyPostingExtensionExpiringSoon(User user, LocalDate expiryDate, int daysAhead);

    void notifyDailyLogMissing(Batch batch, LocalDate date);

    void notifyMedicineAlarmDue(BatchAlarm alarm);

    void notifyFeedbackTicketResolved(FeedbackTicket ticket);

    void notifyAdminWarningIssued(UserWarning warning);

    void notifyAccountUpgradeApproved(AccountUpgradeRequest request);

    void notifyAccountUpgradeRejected(AccountUpgradeRequest request);

    // The caller's own notifications, most recent first.
    Page<NotificationResponse> listForUser(Long userId, Pageable pageable);

    long unreadCount(Long userId);

    // Ownership-checked: throws ResourceNotFoundException if notificationId
    // doesn't belong to userId.
    NotificationResponse markAsRead(Long userId, Long notificationId);
}
