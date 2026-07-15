package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.systemconfig.SystemConfigurationResponse;
import com.poultry.broiler_farming_system.dto.systemconfig.UpsertSystemConfigurationRequest;
import com.poultry.broiler_farming_system.security.UserPrincipal;
import com.poultry.broiler_farming_system.service.systemconfig.SystemConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Admin-only CRUD over system_configurations (medicine dosage/price
// formulas, duration constants, etc). Access is restricted to ROLE_ADMIN by
// the existing "/api/v1/admin/**" rule in SecurityConfig.
@RestController
@RequestMapping("/api/v1/admin/system-configurations")
@RequiredArgsConstructor
public class AdminSystemConfigurationController {

    private final SystemConfigurationService systemConfigurationService;

    @GetMapping
    public List<SystemConfigurationResponse> listAll() {
        return systemConfigurationService.listAll();
    }

    @GetMapping("/{configKey}")
    public SystemConfigurationResponse getByKey(@PathVariable String configKey) {
        return systemConfigurationService.getByKey(configKey);
    }

    // e.g. { "configKey": "default_farming_cycle_duration_days", "configValue": "42", "description": "..." }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SystemConfigurationResponse create(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody UpsertSystemConfigurationRequest request) {
        return systemConfigurationService.create(principal.getId(), request);
    }

    @PutMapping("/{id}")
    public SystemConfigurationResponse update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestBody UpsertSystemConfigurationRequest request) {
        return systemConfigurationService.update(id, principal.getId(), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        systemConfigurationService.delete(id, principal.getId());
    }
}
