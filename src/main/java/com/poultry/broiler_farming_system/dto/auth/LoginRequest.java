package com.poultry.broiler_farming_system.dto.auth;

public record LoginRequest(String usernameOrEmail, String password) {
}
