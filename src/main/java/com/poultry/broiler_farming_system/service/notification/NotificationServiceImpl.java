package com.poultry.broiler_farming_system.service.notification;

import com.poultry.broiler_farming_system.dto.notification.NotificationResponse;
import com.poultry.broiler_farming_system.entity.AccountUpgradeRequest;
import com.poultry.broiler_farming_system.entity.Batch;
import com.poultry.broiler_farming_system.entity.BatchAlarm;
import com.poultry.broiler_farming_system.entity.FeedbackTicket;
import com.poultry.broiler_farming_system.entity.Notification;
import com.poultry.broiler_farming_system.entity.PaymentTransaction;
import com.poultry.broiler_farming_system.entity.User;
import com.poultry.broiler_farming_system.entity.UserWarning;
import com.poultry.broiler_farming_system.entity.enums.NotificationType;
import com.poultry.broiler_farming_system.entity.enums.PaymentType;
import com.poultry.broiler_farming_system.exception.ResourceNotFoundException;
import com.poultry.broiler_farming_system.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void notifyPaymentApproved(PaymentTransaction transaction) {
        boolean isRegistration = transaction.getPaymentType() == PaymentType.BATCH_REGISTRATION;
        String batchName = transaction.getBatch().getBatchName();
        String messageEn = isRegistration
                ? "Your batch registration payment for '" + batchName + "' has been approved. You can now start the batch."
                : "Your posting extension payment has been approved. Your posting privilege has been extended.";
        String messageMy = isRegistration
                ? "သင်၏ '" + batchName + "' အတွက် batch registration ငွေပေးချေမှုကို အတည်ပြုပြီးဖြစ်သည်။ ယခု batch ကို စတင်နိုင်ပါပြီ။"
                : "သင်၏ posting extension ငွေပေးချေမှုကို အတည်ပြုပြီးဖြစ်သည်။ သင်၏ post တင်ခွင့် သက်တမ်းတိုးမြှင့်ပြီးဖြစ်သည်။";

        save(transaction.getUser(), NotificationType.PAYMENT_APPROVED,
                "Payment Approved", "ငွေပေးချေမှု အတည်ပြုပြီး",
                messageEn, messageMy,
                "PAYMENT_TRANSACTION", transaction.getId());
    }

    @Override
    public void notifyPaymentRejected(PaymentTransaction transaction) {
        String batchName = transaction.getBatch().getBatchName();
        String messageEn = "Your " + transaction.getPaymentType() + " payment for batch '" + batchName
                + "' was rejected. Please review your screenshot and submit again.";
        String messageMy = "သင်၏ '" + batchName + "' batch အတွက် ငွေပေးချေမှုကို ပယ်ချလိုက်ပါသည်။ "
                + "Screenshot ကို ပြန်စစ်ပြီး ထပ်မံတင်ပေးပါ။";

        save(transaction.getUser(), NotificationType.PAYMENT_REJECTED,
                "Payment Rejected", "ငွေပေးချေမှု ပယ်ချခံရ",
                messageEn, messageMy,
                "PAYMENT_TRANSACTION", transaction.getId());
    }

    @Override
    public void notifyBatchAutoCompleted(Batch batch) {
        String messageEn = "Your batch '" + batch.getBatchName() + "' has reached the end of its "
                + batch.getCycleDurationDays() + "-day cycle and has been automatically marked as completed.";
        String messageMy = "သင်၏ '" + batch.getBatchName() + "' batch သည် " + batch.getCycleDurationDays()
                + " ရက် သက်တမ်း ပြည့်သွားသဖြင့် အလိုအလျောက် ပြီးစီးအဖြစ် သတ်မှတ်လိုက်ပါသည်။";

        save(batch.getFarmer(), NotificationType.BATCH_AUTO_COMPLETED,
                "Batch Completed", "Batch ပြီးစီးပြီး",
                messageEn, messageMy,
                "BATCH", batch.getId());
    }

    @Override
    public void notifyBatchCycleEndingSoon(Batch batch, LocalDate cycleEndDate, int daysAhead) {
        String messageEn = "Your batch '" + batch.getBatchName() + "' cycle ends in " + daysAhead
                + " days (" + cycleEndDate + ").";
        String messageMy = "သင်၏ '" + batch.getBatchName() + "' batch သက်တမ်းသည် ရက် " + daysAhead
                + " အတွင်း ကုန်ဆုံးပါတော့မည် (" + cycleEndDate + ")။";

        save(batch.getFarmer(), NotificationType.BATCH_CYCLE_ENDING_SOON,
                "Batch Cycle Ending Soon", "Batch သက်တမ်း မကြာမီ ကုန်ဆုံးတော့မည်",
                messageEn, messageMy,
                "BATCH", batch.getId());
    }

    @Override
    public void notifyPostingExtensionExpiringSoon(User user, LocalDate expiryDate, int daysAhead) {
        String messageEn = "Your posting privilege expires in " + daysAhead + " days (" + expiryDate + ").";
        String messageMy = "သင်၏ post တင်ခွင့် သက်တမ်းသည် ရက် " + daysAhead + " အတွင်း ကုန်ဆုံးပါတော့မည် ("
                + expiryDate + ")။";

        save(user, NotificationType.POSTING_EXTENSION_EXPIRING_SOON,
                "Posting Privilege Expiring Soon", "Post တင်ခွင့် သက်တမ်း မကြာမီ ကုန်ဆုံးတော့မည်",
                messageEn, messageMy,
                "USER", user.getId());
    }

    @Override
    public void notifyDailyLogMissing(Batch batch, LocalDate date) {
        String messageEn = "Reminder: today's mortality count and remaining chicken count for batch '"
                + batch.getBatchName() + "' has not been logged yet.";
        String messageMy = "သတိပေးချက်: '" + batch.getBatchName()
                + "' batch အတွက် ယနေ့ ကြက်သေဆုံးအရေအတွက်နှင့် ကျန်ရှိကြက်အရေအတွက်ကို မှတ်တမ်းတင်ရသေးပါ။";

        save(batch.getFarmer(), NotificationType.DAILY_LOG_MISSING,
                "Daily Log Missing", "နေ့စဉ်မှတ်တမ်း မတင်ရသေးပါ",
                messageEn, messageMy,
                "BATCH", batch.getId());

        // The only trigger the original spec requires an SMS for. Deferred to
        // after this transaction commits and run off-thread -- see
        // SmsRequestedEventListener's Javadoc for why a slow/unreliable SMS
        // gateway must never sit inline with this write.
        eventPublisher.publishEvent(new SmsRequestedEvent(batch.getFarmer().getPhoneNumber(), messageEn));
    }

    @Override
    public void notifyMedicineAlarmDue(BatchAlarm alarm) {
        Batch batch = alarm.getBatch();
        String messageEn = "Medicine alarm due for batch '" + batch.getBatchName() + "': '"
                + alarm.getMedicineName() + "' needs to be administered.";
        String messageMy = "'" + batch.getBatchName() + "' batch အတွက် ဆေးဝါးသတိပေးချက် ရောက်ရှိပါပြီ: '"
                + alarm.getMedicineName() + "' ကို ကျွေးရန် လိုအပ်ပါသည်။";

        save(batch.getFarmer(), NotificationType.MEDICINE_ALARM_DUE,
                "Medicine Alarm Due", "ဆေးဝါးသတိပေးချက် ရောက်ရှိပြီ",
                messageEn, messageMy,
                "BATCH_ALARM", alarm.getId());
    }

    @Override
    public void notifyFeedbackTicketResolved(FeedbackTicket ticket) {
        save(ticket.getSubmittedBy(), NotificationType.FEEDBACK_TICKET_RESOLVED,
                "Feedback Ticket Resolved", "အကြံပြုချက် Ticket ဖြေရှင်းပြီး",
                "Your feedback ticket has been resolved by an admin.",
                "သင်၏ အကြံပြုချက် Ticket ကို admin မှ ဖြေရှင်းပြီးဖြစ်သည်။",
                "FEEDBACK_TICKET", ticket.getId());
    }

    @Override
    public void notifyAdminWarningIssued(UserWarning warning) {
        save(warning.getRecipient(), NotificationType.ADMIN_WARNING_ISSUED,
                "Warning Issued", "သတိပေးချက် ထုတ်ပြန်ခံရ",
                "An admin has issued you a warning. Reason: " + warning.getReason(),
                "Admin မှ သင့်ကို သတိပေးချက် ထုတ်ပြန်လိုက်ပါသည်။ အကြောင်းရင်း: " + warning.getReason(),
                "USER_WARNING", warning.getId());
    }

    @Override
    public void notifyAccountUpgradeApproved(AccountUpgradeRequest request) {
        save(request.getRequestedBy(), NotificationType.ACCOUNT_UPGRADE_APPROVED,
                "Farmer Upgrade Approved", "တောင်သူအဆင့်တင်ခြင်း အတည်ပြုပြီး",
                "Your request to become a Farmer has been approved. You now have access to the Farmer tools.",
                "တောင်သူအဖြစ် အဆင့်တင်ရန် သင်၏ တောင်းဆိုချက်ကို အတည်ပြုပြီးဖြစ်သည်။ ယခု Farmer ကိရိယာများကို အသုံးပြုနိုင်ပါပြီ။",
                "ACCOUNT_UPGRADE_REQUEST", request.getId());
    }

    @Override
    public void notifyAccountUpgradeRejected(AccountUpgradeRequest request) {
        String reasonSuffix = request.getAdminNote() != null && !request.getAdminNote().isBlank()
                ? " Reason: " + request.getAdminNote()
                : "";
        String reasonSuffixMy = request.getAdminNote() != null && !request.getAdminNote().isBlank()
                ? " အကြောင်းရင်း: " + request.getAdminNote()
                : "";
        save(request.getRequestedBy(), NotificationType.ACCOUNT_UPGRADE_REJECTED,
                "Farmer Upgrade Rejected", "တောင်သူအဆင့်တင်ခြင်း ပယ်ချခံရ",
                "Your request to become a Farmer was rejected." + reasonSuffix,
                "တောင်သူအဖြစ် အဆင့်တင်ရန် သင်၏ တောင်းဆိုချက်ကို ပယ်ချလိုက်ပါသည်။" + reasonSuffixMy,
                "ACCOUNT_UPGRADE_REQUEST", request.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> listForUser(Long userId, Pageable pageable) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    @Override
    public NotificationResponse markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndRecipientId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification " + notificationId + " was not found for this user."));
        notification.setIsRead(true);
        return toResponse(notification);
    }

    private void save(User recipient, NotificationType type, String titleEn, String titleMy,
                       String messageEn, String messageMy, String relatedEntityType, Long relatedEntityId) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setTitleEn(titleEn);
        notification.setTitleMy(titleMy);
        notification.setMessageEn(messageEn);
        notification.setMessageMy(messageMy);
        notification.setRelatedEntityType(relatedEntityType);
        notification.setRelatedEntityId(relatedEntityId);
        // isRead starts false via the entity's own field default.
        notificationRepository.save(notification);
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitleEn(),
                notification.getTitleMy(),
                notification.getMessageEn(),
                notification.getMessageMy(),
                notification.getIsRead(),
                notification.getRelatedEntityType(),
                notification.getRelatedEntityId(),
                notification.getCreatedAt());
    }
}
