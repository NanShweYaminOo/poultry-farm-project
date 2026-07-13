package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.user.ChangePasswordRequest;
import com.poultry.broiler_farming_system.dto.user.ProfileResponse;
import com.poultry.broiler_farming_system.dto.user.UpdateProfileRequest;
import com.poultry.broiler_farming_system.security.UserPrincipal;
import com.poultry.broiler_farming_system.service.user.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    public ProfileResponse getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return userProfileService.getProfile(principal.getId());
    }

    @PutMapping
    public ProfileResponse updateProfile(
            @AuthenticationPrincipal UserPrincipal principal, @RequestBody UpdateProfileRequest request) {
        return userProfileService.updateProfile(principal.getId(), request);
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @AuthenticationPrincipal UserPrincipal principal, @RequestBody ChangePasswordRequest request) {
        userProfileService.changePassword(principal.getId(), request);
    }

    @PostMapping(value = "/photo", consumes = "multipart/form-data")
    public ProfileResponse updatePhoto(
            @AuthenticationPrincipal UserPrincipal principal, @RequestParam("photo") MultipartFile photo) {
        return userProfileService.updatePhoto(principal.getId(), photo);
    }
}
