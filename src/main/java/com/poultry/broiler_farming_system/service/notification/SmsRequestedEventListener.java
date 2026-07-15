package com.poultry.broiler_farming_system.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Deliberately the only ApplicationEvent/listener pair in this codebase --
 * see SystemLogServiceImpl's Javadoc for why every other cross-cutting
 * side effect here is a plain injected-bean call instead. That reasoning
 * doesn't transfer to this one case: SystemLogService's write is a fast,
 * reliable, local DB insert that SHOULD be atomic with the action it
 * records, so a direct call in the same transaction is strictly simpler
 * and safer. An SMS send is the opposite -- a slow, unreliable, external
 * network call to a third-party gateway -- so it must NOT be atomic with
 * the business transaction that triggered it:
 *  - AFTER_COMMIT means a payment approval (or any other write) can never
 *    be rolled back by an SMS gateway timeout or error, and the gateway is
 *    never called for a write that ends up rolling back for an unrelated
 *    reason.
 *  - @Async means the caller (e.g. PaymentTransactionServiceImpl.reviewPayment,
 *    or DailyLogEnforcementCronJob looping over every active batch) never
 *    blocks on the gateway's round-trip.
 *  - fallbackExecution=true covers callers with no ambient transaction at
 *    all (DailyLogEnforcementCronJob and PremiumExpiryCronJob are plain
 *    @Component beans, not @Transactional) by running immediately (still
 *    off-thread via @Async) instead of silently dropping the event, which
 *    is @TransactionalEventListener's default behavior with no active
 *    transaction.
 *
 * An outbox table (persist the pending SMS, poll/relay it in a separate
 * process) would add durability across a crash between commit and send,
 * but that guarantee isn't worth the extra table + poller at this app's
 * scale (single instance, best-effort SMS reminders) -- a dropped
 * notification on the rare crash-during-send window is acceptable, and
 * the next day's 18:00 run naturally retries for still-missing logs.
 */
@Component
public class SmsRequestedEventListener {

    private static final Logger log = LoggerFactory.getLogger(SmsRequestedEventListener.class);

    private final SmsService smsService;

    public SmsRequestedEventListener(SmsService smsService) {
        this.smsService = smsService;
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onSmsRequested(SmsRequestedEvent event) {
        try {
            smsService.sendSms(event.phoneNumber(), event.message());
        } catch (RuntimeException ex) {
            // Never propagate -- this already runs after commit, off the
            // original caller's thread, so there is nothing left upstream
            // that a thrown exception here could usefully affect. Just log
            // it so a failed delivery is visible in the server console.
            log.error("Failed to deliver SMS: {}", ex.getMessage(), ex);
        }
    }
}
