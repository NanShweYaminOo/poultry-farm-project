package com.poultry.broiler_farming_system.service.systemlog;

import com.poultry.broiler_farming_system.dto.systemlog.SystemLogResponse;
import com.poultry.broiler_farming_system.entity.enums.SystemLogAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SystemLogService {

    // Called explicitly by every admin moderation service (AdminUserService,
    // SalesPostService, BuyRequestService, GroupChatService) right after
    // their action succeeds -- see the class Javadoc on
    // SystemLogServiceImpl for why this is a plain service call rather than
    // an @Aspect or an ApplicationEvent.
    void record(Long adminId, SystemLogAction action, String targetType, Long targetId, String description);

    Page<SystemLogResponse> listLogs(Pageable pageable);
}
