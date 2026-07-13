package com.poultry.broiler_farming_system.service.breed;

import com.poultry.broiler_farming_system.dto.breed.BreedResponse;
import com.poultry.broiler_farming_system.dto.breed.UpsertBreedRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BreedService {

    List<BreedResponse> listBreeds();

    BreedResponse getBreed(Long breedId);

    BreedResponse createBreed(UpsertBreedRequest request, MultipartFile image);

    // image may be null/empty to leave the existing photo (if any) untouched.
    BreedResponse updateBreed(Long breedId, UpsertBreedRequest request, MultipartFile image);

    void deleteBreed(Long breedId);
}
