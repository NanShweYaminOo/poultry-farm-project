package com.poultry.broiler_farming_system.service.auth;

import com.poultry.broiler_farming_system.dto.auth.AuthResponse;
import com.poultry.broiler_farming_system.dto.auth.LoginRequest;
import com.poultry.broiler_farming_system.dto.auth.RegisterRequest;
import com.poultry.broiler_farming_system.entity.User;
import com.poultry.broiler_farming_system.entity.enums.AccountType;
import com.poultry.broiler_farming_system.exception.AccountBannedException;
import com.poultry.broiler_farming_system.exception.DuplicateResourceException;
import com.poultry.broiler_farming_system.exception.InvalidCredentialsException;
import com.poultry.broiler_farming_system.repository.UserRepository;
import com.poultry.broiler_farming_system.security.JwtService;
import com.poultry.broiler_farming_system.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        validateRegisterRequest(request);

        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username '" + request.username() + "' is already taken.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email '" + request.email() + "' is already registered.");
        }

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setUsername(request.username().trim());
        user.setPhoneNumber(request.phoneNumber().trim());
        user.setEmail(request.email().trim());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setLocation(request.location());
        if (StringUtils.hasText(request.preferredLanguage())) {
            user.setPreferredLanguage(request.preferredLanguage().trim());
        }
        user.setAccountType(parseAccountType(request.accountType()));
        // role=FREE, isBanned/isFlaggedForReview=false via entity field defaults.

        User saved = userRepository.save(user);
        return toAuthResponse(new UserPrincipal(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.usernameOrEmail(), request.password()));
        } catch (DisabledException ex) {
            throw new AccountBannedException("This account has been banned.");
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException("Invalid username/email or password.");
        }

        return toAuthResponse((UserPrincipal) authentication.getPrincipal());
    }

    private void validateRegisterRequest(RegisterRequest request) {
        requireText(request.fullName(), "fullName");
        requireText(request.username(), "username");
        requireText(request.phoneNumber(), "phoneNumber");
        requireText(request.email(), "email");
        requireText(request.password(), "password");
        if (request.password().length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
    }

    private void requireText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
    }

    private AccountType parseAccountType(String value) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("accountType is required.");
        }
        try {
            return AccountType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("accountType must be GUEST or FARMER.");
        }
    }

    private AuthResponse toAuthResponse(UserPrincipal principal) {
        String token = jwtService.generateToken(principal);
        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationSeconds(),
                principal.getId(),
                principal.getUsername(),
                principal.getUser().getRole(),
                principal.getUser().getAccountType()
        );
    }
}
