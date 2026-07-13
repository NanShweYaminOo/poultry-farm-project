package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.breed.BreedResponse;
import com.poultry.broiler_farming_system.dto.breed.UpsertBreedRequest;
import com.poultry.broiler_farming_system.service.breed.BreedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/breeds")
@RequiredArgsConstructor
public class BreedController {

    private final BreedService breedService;

    // Any authenticated user (breed reference data feeds cost-estimation/guidance
    // features app-wide, not just the admin panel) -- see anyRequest().authenticated()
    // in SecurityConfig. Only the write endpoints below are ADMIN-restricted.
    @GetMapping
    public List<BreedResponse> listBreeds() {
        return breedService.listBreeds();
    }

    @GetMapping("/{breedId}")
    public BreedResponse getBreed(@PathVariable Long breedId) {
        return breedService.getBreed(breedId);
    }

    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public BreedResponse createBreed(
            @ModelAttribute UpsertBreedRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        return breedService.createBreed(request, image);
    }

    @PutMapping(value = "/{breedId}", consumes = "multipart/form-data")
    public BreedResponse updateBreed(
            @PathVariable Long breedId,
            @ModelAttribute UpsertBreedRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        return breedService.updateBreed(breedId, request, image);
    }

    @DeleteMapping("/{breedId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBreed(@PathVariable Long breedId) {
        breedService.deleteBreed(breedId);
    }
}
