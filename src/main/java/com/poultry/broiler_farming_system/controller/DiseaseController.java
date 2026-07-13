package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.disease.DiseaseResponse;
import com.poultry.broiler_farming_system.dto.disease.UpsertDiseaseRequest;
import com.poultry.broiler_farming_system.service.disease.DiseaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/diseases")
@RequiredArgsConstructor
public class DiseaseController {

    private final DiseaseService diseaseService;

    // Any authenticated user (disease reference data feeds the chatbot
    // diagnosis feature app-wide) -- see anyRequest().authenticated() in
    // SecurityConfig. Only the write endpoints below are ADMIN-restricted.
    @GetMapping
    public List<DiseaseResponse> listDiseases() {
        return diseaseService.listDiseases();
    }

    @GetMapping("/{diseaseId}")
    public DiseaseResponse getDisease(@PathVariable Long diseaseId) {
        return diseaseService.getDisease(diseaseId);
    }

    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public DiseaseResponse createDisease(
            @ModelAttribute UpsertDiseaseRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        return diseaseService.createDisease(request, image);
    }

    @PutMapping(value = "/{diseaseId}", consumes = "multipart/form-data")
    public DiseaseResponse updateDisease(
            @PathVariable Long diseaseId,
            @ModelAttribute UpsertDiseaseRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        return diseaseService.updateDisease(diseaseId, request, image);
    }

    @DeleteMapping("/{diseaseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDisease(@PathVariable Long diseaseId) {
        diseaseService.deleteDisease(diseaseId);
    }
}
