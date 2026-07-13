package com.poultry.broiler_farming_system.service.disease;

import com.poultry.broiler_farming_system.dto.disease.DiseaseResponse;
import com.poultry.broiler_farming_system.dto.disease.UpsertDiseaseRequest;
import com.poultry.broiler_farming_system.entity.Disease;
import com.poultry.broiler_farming_system.entity.enums.DiseaseSeverity;
import com.poultry.broiler_farming_system.exception.ResourceNotFoundException;
import com.poultry.broiler_farming_system.repository.DiseaseRepository;
import com.poultry.broiler_farming_system.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DiseaseServiceImpl implements DiseaseService {

    private final DiseaseRepository diseaseRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public List<DiseaseResponse> listDiseases() {
        return diseaseRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DiseaseResponse getDisease(Long diseaseId) {
        return toResponse(findDisease(diseaseId));
    }

    @Override
    public DiseaseResponse createDisease(UpsertDiseaseRequest request, MultipartFile image) {
        requireText(request.name(), "name");

        Disease disease = new Disease();
        applyFields(disease, request);
        disease.setSeverity(request.severity() != null ? request.severity() : DiseaseSeverity.MODERATE);
        if (image != null && !image.isEmpty()) {
            disease.setImageUrl(fileStorageService.store(image, "disease-photos", "disease"));
        }

        return toResponse(diseaseRepository.save(disease));
    }

    @Override
    public DiseaseResponse updateDisease(Long diseaseId, UpsertDiseaseRequest request, MultipartFile image) {
        requireText(request.name(), "name");

        Disease disease = findDisease(diseaseId);
        applyFields(disease, request);
        if (request.severity() != null) {
            disease.setSeverity(request.severity());
        }

        if (image != null && !image.isEmpty()) {
            String previousUrl = disease.getImageUrl();
            disease.setImageUrl(fileStorageService.store(image, "disease-photos", "disease-" + diseaseId));
            fileStorageService.delete(previousUrl);
        }

        return toResponse(diseaseRepository.save(disease));
    }

    @Override
    public void deleteDisease(Long diseaseId) {
        Disease disease = findDisease(diseaseId);
        diseaseRepository.delete(disease);
        fileStorageService.delete(disease.getImageUrl());
    }

    private void applyFields(Disease disease, UpsertDiseaseRequest request) {
        disease.setName(request.name().trim());
        disease.setKeySymptoms(request.keySymptoms());
        disease.setNotes(request.notes());
    }

    private Disease findDisease(Long diseaseId) {
        return diseaseRepository.findById(diseaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease " + diseaseId + " was not found."));
    }

    private void requireText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
    }

    private DiseaseResponse toResponse(Disease disease) {
        return new DiseaseResponse(
                disease.getId(),
                disease.getName(),
                disease.getKeySymptoms(),
                disease.getSeverity(),
                disease.getNotes(),
                disease.getImageUrl(),
                disease.getCreatedDate()
        );
    }
}
