package com.poultry.broiler_farming_system.service.user;

import com.poultry.broiler_farming_system.dto.user.ChangePasswordRequest;
import com.poultry.broiler_farming_system.dto.user.ProfileResponse;
import com.poultry.broiler_farming_system.dto.user.UpdateProfileRequest;
import org.springframework.web.multipart.MultipartFile;

public interface UserProfileService {

    ProfileResponse getProfile(Long userId);

    ProfileResponse updateProfile(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    ProfileResponse updatePhoto(Long userId, MultipartFile photo);
}
