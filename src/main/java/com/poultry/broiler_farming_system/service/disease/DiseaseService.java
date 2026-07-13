package com.poultry.broiler_farming_system.service.disease;

import com.poultry.broiler_farming_system.dto.disease.DiseaseResponse;
import com.poultry.broiler_farming_system.dto.disease.UpsertDiseaseRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DiseaseService {

    List<DiseaseResponse> listDiseases();

    DiseaseResponse getDisease(Long diseaseId);

    DiseaseResponse createDisease(UpsertDiseaseRequest request, MultipartFile image);

    DiseaseResponse updateDisease(Long diseaseId, UpsertDiseaseRequest request, MultipartFile image);

    void deleteDisease(Long diseaseId);
}
