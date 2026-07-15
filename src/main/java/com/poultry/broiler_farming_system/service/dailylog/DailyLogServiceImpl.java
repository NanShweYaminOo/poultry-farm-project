package com.poultry.broiler_farming_system.service.dailylog;

import com.poultry.broiler_farming_system.dto.dailylog.CreateDailyLogRequest;
import com.poultry.broiler_farming_system.dto.dailylog.DailyLogResponse;
import com.poultry.broiler_farming_system.dto.dailylog.DailyLogTodayStatusResponse;
import com.poultry.broiler_farming_system.entity.Batch;
import com.poultry.broiler_farming_system.entity.DailyLog;
import com.poultry.broiler_farming_system.entity.User;
import com.poultry.broiler_farming_system.entity.enums.BatchStatus;
import com.poultry.broiler_farming_system.entity.enums.UserRole;
import com.poultry.broiler_farming_system.exception.DuplicateResourceException;
import com.poultry.broiler_farming_system.exception.InvalidBatchStateException;
import com.poultry.broiler_farming_system.exception.ResourceNotFoundException;
import com.poultry.broiler_farming_system.exception.UnauthorizedActionException;
import com.poultry.broiler_farming_system.repository.BatchRepository;
import com.poultry.broiler_farming_system.repository.DailyLogRepository;
import com.poultry.broiler_farming_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DailyLogServiceImpl implements DailyLogService {

    private final DailyLogRepository dailyLogRepository;
    private final BatchRepository batchRepository;
    private final UserRepository userRepository;

    @Override
    public DailyLogResponse create(Long farmerId, CreateDailyLogRequest request) {
        if (request.batchId() == null) {
            throw new IllegalArgumentException("batchId is required.");
        }
        if (request.dailyMortalityCount() == null || request.dailyMortalityCount() < 0) {
            throw new IllegalArgumentException("dailyMortalityCount is required and cannot be negative.");
        }
        if (request.totalRemainingChickenCount() == null || request.totalRemainingChickenCount() < 0) {
            throw new IllegalArgumentException("totalRemainingChickenCount is required and cannot be negative.");
        }

        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + farmerId + " was not found."));
        Batch batch = batchRepository.findById(request.batchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch " + request.batchId() + " was not found."));

        if (!batch.getFarmer().getId().equals(farmer.getId())) {
            throw new IllegalArgumentException(
                    "You are not the farmer on batch " + batch.getId() + "; daily log rejected.");
        }
        if (!Boolean.TRUE.equals(batch.getIsStarted()) || batch.getStatus() != BatchStatus.ACTIVE) {
            throw new InvalidBatchStateException(
                    "Batch " + batch.getId() + " is not an active, started batch; daily logs cannot be recorded.");
        }

        LocalDate logDate = request.logDate() != null ? request.logDate() : LocalDate.now();
        if (logDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("logDate cannot be in the future.");
        }
        if (dailyLogRepository.existsByBatchIdAndLogDate(batch.getId(), logDate)) {
            throw new DuplicateResourceException(
                    "A daily log for batch " + batch.getId() + " on " + logDate + " already exists.");
        }

        // Validate against the latest recorded count (or the batch's
        // starting count if this is the first log and one was set) -- a
        // batch can only ever lose birds day over day, never gain them.
        // initialChickenCount is optional at batch creation, so there may be
        // no baseline to compare against yet.
        Integer previousCount = dailyLogRepository.findTopByBatchIdOrderByLogDateDesc(batch.getId())
                .map(DailyLog::getTotalRemainingChickenCount)
                .orElse(batch.getInitialChickenCount());
        if (previousCount != null) {
            if (request.totalRemainingChickenCount() > previousCount) {
                throw new IllegalArgumentException(
                        "totalRemainingChickenCount cannot exceed the previous recorded count of " + previousCount + ".");
            }
            if (request.dailyMortalityCount() > previousCount) {
                throw new IllegalArgumentException(
                        "dailyMortalityCount cannot exceed the previous recorded count of " + previousCount + ".");
            }
        }

        DailyLog dailyLog = DailyLog.builder()
                .batch(batch)
                .logDate(logDate)
                .dailyMortalityCount(request.dailyMortalityCount())
                .totalRemainingChickenCount(request.totalRemainingChickenCount())
                .build();

        return toResponse(dailyLogRepository.save(dailyLog));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyLogResponse> listByBatch(Long callerId, Long batchId) {
        User caller = userRepository.findById(callerId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + callerId + " was not found."));
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch " + batchId + " was not found."));

        if (!batch.getFarmer().getId().equals(caller.getId()) && caller.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedActionException(
                    "User " + caller.getId() + " may not view daily logs for batch " + batch.getId() + ".");
        }

        return dailyLogRepository.findByBatchIdOrderByLogDateDesc(batchId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DailyLogTodayStatusResponse getTodayStatus(Long callerId, Long batchId) {
        User caller = userRepository.findById(callerId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + callerId + " was not found."));
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch " + batchId + " was not found."));

        if (!batch.getFarmer().getId().equals(caller.getId()) && caller.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedActionException(
                    "User " + caller.getId() + " may not view daily logs for batch " + batch.getId() + ".");
        }

        LocalDate today = LocalDate.now();
        DailyLog todayLog = dailyLogRepository.findByBatchIdAndLogDate(batchId, today).orElse(null);
        return new DailyLogTodayStatusResponse(
                batchId, today, todayLog != null, todayLog != null ? toResponse(todayLog) : null);
    }

    private DailyLogResponse toResponse(DailyLog dailyLog) {
        Batch batch = dailyLog.getBatch();
        return new DailyLogResponse(
                dailyLog.getId(),
                batch.getId(),
                batch.getBatchName(),
                dailyLog.getLogDate(),
                dailyLog.getDailyMortalityCount(),
                dailyLog.getTotalRemainingChickenCount());
    }
}
