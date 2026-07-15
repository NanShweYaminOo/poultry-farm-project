package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.systemlog.SystemLogResponse;
import com.poultry.broiler_farming_system.service.systemlog.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Backs the admin/logs page. Access is restricted to ROLE_ADMIN by the
// existing "/api/v1/admin/**" rule in SecurityConfig.
@RestController
@RequestMapping("/api/v1/admin/system-logs")
@RequiredArgsConstructor
public class AdminSystemLogController {

    private final SystemLogService systemLogService;

    @GetMapping
    public Page<SystemLogResponse> listLogs(
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return systemLogService.listLogs(pageable);
    }
}
