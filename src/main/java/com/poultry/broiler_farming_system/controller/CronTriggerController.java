package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.scheduling.DailyLogEnforcementCronJob;
import com.poultry.broiler_farming_system.scheduling.PremiumExpiryCronJob;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Manual "run now" trigger for the two fixed-schedule cron jobs -- an ops
// convenience so an admin (or a test) doesn't have to wait for the actual
// midnight/18:00 firing. Both jobs are otherwise fully automatic via
// @Scheduled.
@RestController
@RequestMapping("/api/v1/admin/cron")
@RequiredArgsConstructor
public class CronTriggerController {

    private final PremiumExpiryCronJob premiumExpiryCronJob;
    private final DailyLogEnforcementCronJob dailyLogEnforcementCronJob;

    @PostMapping("/premium-expiry-scan")
    public void runPremiumExpiryScan() {
        premiumExpiryCronJob.runDailyExpiryScan();
    }

    @PostMapping("/daily-log-enforcement")
    public void runDailyLogEnforcement() {
        dailyLogEnforcementCronJob.enforceDailyLogging();
    }
}
