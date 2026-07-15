package com.poultry.broiler_farming_system.service.batch;

import com.poultry.broiler_farming_system.dto.batch.BatchResponse;
import com.poultry.broiler_farming_system.dto.batch.CreateBatchRequest;
import com.poultry.broiler_farming_system.entity.enums.BatchStatus;

import java.util.List;

public interface BatchService {

    /**
     * Batches belonging to the given farmer, most recently created first.
     */
    List<BatchResponse> listMyBatches(Long farmerId);

    /**
     * Farmer requests a new batch. Multiple concurrent batches per farmer
     * are allowed -- no check against existing active batches. Not yet
     * admin-approved or started; a payment_transactions row (and its
     * approval) must follow before "Start Batch" is possible.
     */
    BatchResponse createBatch(Long farmerId, CreateBatchRequest request);

    /**
     * "Start Batch": the farming cycle clock does not start at admin
     * payment approval -- only when the farmer explicitly clicks Start.
     * Requires the batch to already be admin-approved and not already
     * started; sets is_started=true and start_date=today. callerId must own
     * the batch (or be an Admin).
     */
    BatchResponse startBatch(Long callerId, Long batchId);

    /**
     * "Stop Batch": terminates or successfully completes an active batch
     * early. Instantly evicts every pending Quartz medicine alarm for the
     * batch and records the final lifecycle status; the batch is locked
     * from further tracking (no new alarms, no more estimates/completions)
     * once stopped. callerId must own the batch (or be an Admin) -- the
     * daily expiry cron passes the batch's own farmer id here for its
     * auto-completions, so this same ownership-checked path covers both.
     * If this drops the farmer to zero remaining active started batches and
     * they're currently PAID, they're demoted back to FREE (never ADMIN).
     */
    BatchResponse stopBatch(Long callerId, Long batchId, BatchStatus finalStatus);
}
