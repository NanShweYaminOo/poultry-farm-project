package com.poultry.broiler_farming_system.service.systemconfig;

import com.poultry.broiler_farming_system.dto.systemconfig.SystemConfigurationResponse;
import com.poultry.broiler_farming_system.dto.systemconfig.UpsertSystemConfigurationRequest;

import java.util.List;

public interface SystemConfigurationService {

    List<SystemConfigurationResponse> listAll();

    SystemConfigurationResponse getByKey(String configKey);

    SystemConfigurationResponse create(Long adminId, UpsertSystemConfigurationRequest request);

    SystemConfigurationResponse update(Long id, Long adminId, UpsertSystemConfigurationRequest request);

    void delete(Long id, Long adminId);
}
