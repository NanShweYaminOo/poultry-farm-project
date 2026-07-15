package com.poultry.broiler_farming_system.service.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Simple fixed-window token bucket: app.sms.rate-limit-per-second permits
 * are available each second, refilled on a 1s tick. Guards against a single
 * large scan (e.g. DailyLogEnforcementCronJob iterating every active batch
 * at 18:00) bursting past the SMS gateway's own per-second throttle and
 * getting throttled/blacklisted -- hand-rolled rather than pulling in a
 * rate-limiting library, since one Semaphore + one @Scheduled reset is the
 * entire requirement.
 */
@Component
public class SmsRateLimiter {

    private final int permitsPerSecond;
    private final Semaphore semaphore;

    public SmsRateLimiter(@Value("${app.sms.rate-limit-per-second:5}") int permitsPerSecond) {
        this.permitsPerSecond = permitsPerSecond;
        this.semaphore = new Semaphore(permitsPerSecond);
    }

    /**
     * Blocks up to 2 seconds for a free slot. Returns false (never throws)
     * if none frees up in time, so the caller can treat it as just another
     * kind of delivery failure.
     */
    public boolean tryAcquire() {
        try {
            return semaphore.tryAcquire(2, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Scheduled(fixedRate = 1000)
    void refill() {
        semaphore.drainPermits();
        semaphore.release(permitsPerSecond);
    }
}
