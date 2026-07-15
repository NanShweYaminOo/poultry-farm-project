package com.poultry.broiler_farming_system.dto.systemconfig;

import java.time.LocalDateTime;

public record SystemConfigurationResponse(
        Long id,
        String configKey,
        String configValue,
        String description,
        LocalDateTime updatedAt
) {
}
