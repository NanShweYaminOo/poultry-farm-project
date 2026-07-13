package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.auth.AuthResponse;
import com.poultry.broiler_farming_system.dto.auth.LoginRequest;
import com.poultry.broiler_farming_system.dto.auth.RegisterRequest;
import com.poultry.broiler_farming_system.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
