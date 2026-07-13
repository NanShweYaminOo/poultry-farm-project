package com.poultry.broiler_farming_system.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// Enables the fixed-schedule @Scheduled cron jobs (premium expiry warnings,
// daily log enforcement). Separate from Quartz, which is reserved for the
// per-batch dynamic medicine alarms that need runtime create/cancel -- these
// two jobs run on a single static system-wide cron expression each and
// don't need Quartz's persistence/rescheduling machinery.
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
