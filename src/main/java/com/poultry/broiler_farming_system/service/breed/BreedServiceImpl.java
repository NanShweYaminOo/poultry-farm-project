package com.poultry.broiler_farming_system.service.breed;

import com.poultry.broiler_farming_system.dto.breed.BreedResponse;
import com.poultry.broiler_farming_system.dto.breed.UpsertBreedRequest;
import com.poultry.broiler_farming_system.entity.Breed;
import com.poultry.broiler_farming_system.entity.enums.BreedStatus;
import com.poultry.broiler_farming_system.exception.ResourceNotFoundException;
import com.poultry.broiler_farming_system.repository.BreedRepository;
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
public class BreedServiceImpl implements BreedService {

    private final BreedRepository breedRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public List<BreedResponse> listBreeds() {
        return breedRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BreedResponse getBreed(Long breedId) {
        return toResponse(findBreed(breedId));
    }

    @Override
    public BreedResponse createBreed(UpsertBreedRequest request, MultipartFile image) {
        requireText(request.name(), "name");

        Breed breed = new Breed();
        applyFields(breed, request);
        breed.setStatus(request.status() != null ? request.status() : BreedStatus.ACTIVE);
        if (image != null && !image.isEmpty()) {
            breed.setImageUrl(fileStorageService.store(image, "breed-photos", "breed"));
        }

        return toResponse(breedRepository.save(breed));
    }

    @Override
    public BreedResponse updateBreed(Long breedId, UpsertBreedRequest request, MultipartFile image) {
        requireText(request.name(), "name");

        Breed breed = findBreed(breedId);
        applyFields(breed, request);
        if (request.status() != null) {
            breed.setStatus(request.status());
        }

        if (image != null && !image.isEmpty()) {
            String previousUrl = breed.getImageUrl();
            breed.setImageUrl(fileStorageService.store(image, "breed-photos", "breed-" + breedId));
            fileStorageService.delete(previousUrl);
        }

        return toResponse(breedRepository.save(breed));
    }

    @Override
    public void deleteBreed(Long breedId) {
        Breed breed = findBreed(breedId);
        breedRepository.delete(breed);
        fileStorageService.delete(breed.getImageUrl());
    }

    private void applyFields(Breed breed, UpsertBreedRequest request) {
        breed.setName(request.name().trim());
        breed.setOrigin(request.origin());
        breed.setAvgMarketWeightKg(request.avgMarketWeightKg());
        breed.setGrowthPeriodDays(request.growthPeriodDays());
        breed.setFcr(request.fcr());
        breed.setDescription(request.description());
    }

    private Breed findBreed(Long breedId) {
        return breedRepository.findById(breedId)
                .orElseThrow(() -> new ResourceNotFoundException("Breed " + breedId + " was not found."));
    }

    private void requireText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
    }

    private BreedResponse toResponse(Breed breed) {
        return new BreedResponse(
                breed.getId(),
                breed.getName(),
                breed.getOrigin(),
                breed.getAvgMarketWeightKg(),
                breed.getGrowthPeriodDays(),
                breed.getFcr(),
                breed.getDescription(),
                breed.getImageUrl(),
                breed.getStatus(),
                breed.getCreatedDate()
        );
    }
}
