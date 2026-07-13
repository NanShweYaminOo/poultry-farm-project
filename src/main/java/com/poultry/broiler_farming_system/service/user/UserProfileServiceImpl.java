package com.poultry.broiler_farming_system.service.user;

import com.poultry.broiler_farming_system.dto.user.ChangePasswordRequest;
import com.poultry.broiler_farming_system.dto.user.ProfileResponse;
import com.poultry.broiler_farming_system.dto.user.UpdateProfileRequest;
import com.poultry.broiler_farming_system.entity.User;
import com.poultry.broiler_farming_system.exception.ResourceNotFoundException;
import com.poultry.broiler_farming_system.repository.UserRepository;
import com.poultry.broiler_farming_system.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileServiceImpl implements UserProfileService {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long userId) {
        return toResponse(findUser(userId));
    }

    @Override
    public ProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        requireText(request.fullName(), "fullName");
        requireText(request.phoneNumber(), "phoneNumber");

        User user = findUser(userId);
        user.setFullName(request.fullName().trim());
        user.setPhoneNumber(request.phoneNumber().trim());
        return toResponse(userRepository.save(user));
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        requireText(request.currentPassword(), "currentPassword");
        requireText(request.newPassword(), "newPassword");
        if (request.newPassword().length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("newPassword must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }

        User user = findUser(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            // Deliberately not InvalidCredentialsException (401): the caller's JWT
            // is valid here, only the submitted currentPassword field is wrong.
            // A 401 would trip AdminUI.authFetch's "session expired" handling and
            // force-redirect to login over what is really just a validation error.
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    public ProfileResponse updatePhoto(Long userId, MultipartFile photo) {
        User user = findUser(userId);
        String previousUrl = user.getProfileImageUrl();

        String newUrl = fileStorageService.store(photo, "profile-photos", "user-" + userId);
        user.setProfileImageUrl(newUrl);
        ProfileResponse response = toResponse(userRepository.save(user));

        fileStorageService.delete(previousUrl);
        return response;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + userId + " was not found."));
    }

    private void requireText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
    }

    private ProfileResponse toResponse(User user) {
        return new ProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getProfileImageUrl(),
                user.getRole()
        );
    }
}
