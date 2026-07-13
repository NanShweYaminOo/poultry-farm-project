package com.poultry.broiler_farming_system.dto.user;

public record ChangePasswordRequest(String currentPassword, String newPassword) {
}
