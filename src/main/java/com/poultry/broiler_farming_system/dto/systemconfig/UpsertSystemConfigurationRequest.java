package com.poultry.broiler_farming_system.dto.systemconfig;

public record UpsertSystemConfigurationRequest(
        String configKey,
        String configValue,
        String description
) {
}
