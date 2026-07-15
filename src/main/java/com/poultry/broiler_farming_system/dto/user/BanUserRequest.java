package com.poultry.broiler_farming_system.dto.user;

// reason is optional -- the request body itself may be omitted entirely
// (see AdminUserController.banUser).
public record BanUserRequest(String reason) {
}
