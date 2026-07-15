package com.poultry.broiler_farming_system.service.systemconfig;

import com.poultry.broiler_farming_system.dto.systemconfig.SystemConfigurationResponse;
import com.poultry.broiler_farming_system.dto.systemconfig.UpsertSystemConfigurationRequest;
import com.poultry.broiler_farming_system.entity.SystemConfiguration;
import com.poultry.broiler_farming_system.entity.User;
import com.poultry.broiler_farming_system.entity.enums.UserRole;
import com.poultry.broiler_farming_system.exception.DuplicateResourceException;
import com.poultry.broiler_farming_system.exception.ResourceNotFoundException;
import com.poultry.broiler_farming_system.exception.UnauthorizedActionException;
import com.poultry.broiler_farming_system.repository.SystemConfigurationRepository;
import com.poultry.broiler_farming_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SystemConfigurationServiceImpl implements SystemConfigurationService {

    private final SystemConfigurationRepository systemConfigurationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SystemConfigurationResponse> listAll() {
        return systemConfigurationRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SystemConfigurationResponse getByKey(String configKey) {
        SystemConfiguration configuration = systemConfigurationRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "System configuration '" + configKey + "' was not found."));
        return toResponse(configuration);
    }

    @Override
    public SystemConfigurationResponse create(Long adminId, UpsertSystemConfigurationRequest request) {
        requireAdmin(adminId, "create");
        requireText(request.configKey(), "configKey");
        requireText(request.configValue(), "configValue");

        String key = request.configKey().trim();
        if (systemConfigurationRepository.findByConfigKey(key).isPresent()) {
            throw new DuplicateResourceException("System configuration '" + key + "' already exists.");
        }

        SystemConfiguration configuration = new SystemConfiguration();
        configuration.setConfigKey(key);
        configuration.setConfigValue(request.configValue().trim());
        configuration.setDescription(request.description());

        return toResponse(systemConfigurationRepository.save(configuration));
    }

    @Override
    public SystemConfigurationResponse update(Long id, Long adminId, UpsertSystemConfigurationRequest request) {
        requireAdmin(adminId, "update");
        requireText(request.configKey(), "configKey");
        requireText(request.configValue(), "configValue");

        SystemConfiguration configuration = findById(id);

        String key = request.configKey().trim();
        systemConfigurationRepository.findByConfigKey(key).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new DuplicateResourceException("System configuration '" + key + "' already exists.");
            }
        });

        configuration.setConfigKey(key);
        configuration.setConfigValue(request.configValue().trim());
        configuration.setDescription(request.description());

        return toResponse(systemConfigurationRepository.save(configuration));
    }

    @Override
    public void delete(Long id, Long adminId) {
        requireAdmin(adminId, "delete");
        systemConfigurationRepository.delete(findById(id));
    }

    private void requireAdmin(Long adminId, String action) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user " + adminId + " was not found."));
        // SecurityConfig already restricts "/api/v1/admin/**" to ROLE_ADMIN;
        // this is a cheap defense-in-depth re-check, not the primary gate.
        if (admin.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedActionException(
                    "User " + admin.getId() + " is not an Admin and cannot " + action + " system configurations.");
        }
    }

    private SystemConfiguration findById(Long id) {
        return systemConfigurationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("System configuration " + id + " was not found."));
    }

    private void requireText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
    }

    private SystemConfigurationResponse toResponse(SystemConfiguration configuration) {
        return new SystemConfigurationResponse(
                configuration.getId(),
                configuration.getConfigKey(),
                configuration.getConfigValue(),
                configuration.getDescription(),
                configuration.getUpdatedAt());
    }
}
