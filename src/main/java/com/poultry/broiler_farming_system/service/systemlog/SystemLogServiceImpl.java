package com.poultry.broiler_farming_system.service.systemlog;

import com.poultry.broiler_farming_system.dto.systemlog.SystemLogResponse;
import com.poultry.broiler_farming_system.entity.SystemLog;
import com.poultry.broiler_farming_system.entity.User;
import com.poultry.broiler_farming_system.entity.enums.SystemLogAction;
import com.poultry.broiler_farming_system.exception.ResourceNotFoundException;
import com.poultry.broiler_farming_system.repository.SystemLogRepository;
import com.poultry.broiler_farming_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * record() deliberately participates in the caller's ambient transaction
 * (plain @Transactional, no REQUIRES_NEW) rather than using
 * UserFlagService's pattern -- that one needs its own committed transaction
 * because a profanity flag must survive the surrounding request being
 * rejected. Here it's the opposite: a log entry should only exist if the
 * action it describes actually committed, so it belongs in the same
 * transaction as the ban/delete/warning/etc it records.
 *
 * On the wider design question (explicit service call vs @Aspect vs
 * ApplicationEvent): this codebase has no AOP or event-listener
 * infrastructure anywhere, and every other "side effect that must reliably
 * happen alongside a primary write" (see UserFlagService) is already done
 * via a plain injected bean called directly from the service method. An
 * @Aspect would need a pointcut matching heterogeneous method signatures
 * across five unrelated services and reflectively reconstruct "who/what/why"
 * from arbitrary arguments -- fragile and opaque compared to just writing
 * the description at the call site where the context already exists.
 * ApplicationEvents would add a layer of indirection with no real benefit
 * here, since there's exactly one producer and one consumer for every action
 * (no fan-out to multiple listeners), so an explicit call is both simpler
 * and easier to trace than events.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SystemLogServiceImpl implements SystemLogService {

    private final SystemLogRepository systemLogRepository;
    private final UserRepository userRepository;

    @Override
    public void record(Long adminId, SystemLogAction action, String targetType, Long targetId, String description) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user " + adminId + " was not found."));

        SystemLog log = new SystemLog();
        log.setAdmin(admin);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDescription(description);
        systemLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SystemLogResponse> listLogs(Pageable pageable) {
        return systemLogRepository.findAll(pageable).map(this::toResponse);
    }

    private SystemLogResponse toResponse(SystemLog log) {
        return new SystemLogResponse(
                log.getId(),
                log.getAdmin().getId(),
                log.getAdmin().getUsername(),
                log.getAction(),
                log.getTargetType(),
                log.getTargetId(),
                log.getDescription(),
                log.getCreatedAt());
    }
}
