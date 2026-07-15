package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.auth.AuthResponse;
import com.poultry.broiler_farming_system.dto.auth.LoginRequest;
import com.poultry.broiler_farming_system.dto.auth.RegisterRequest;
import com.poultry.broiler_farming_system.security.JwtService;
import com.poultry.broiler_farming_system.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Also sets the role-specific cookie (see JwtService.cookieNameFor) so
    // the server-rendered /farmer/** or /guest/** JSP shells are
    // authenticated on page load right after registering, same as login.
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(
            @RequestBody RegisterRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        AuthResponse authResponse = authService.register(request);
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, buildAuthCookie(authResponse, httpRequest).toString());
        return authResponse;
    }

    // Also sets a role-specific HttpOnly cookie (admin_auth_token /
    // farmer_auth_token / guest_auth_token) so the server-rendered
    // /admin/**, /farmer/** and /guest/** JSP shells (which can't attach a
    // Bearer header) get authenticated on page load. One cookie per area
    // rather than a single shared one -- see JwtService's Javadoc for why:
    // it lets a Farmer session in one tab and a Guest session in another
    // coexist instead of clobbering each other. The JSON body is unchanged
    // -- the frontend still stores accessToken in localStorage and sends it
    // as an Authorization header for every /api/** call, cookie or not.
    @PostMapping("/login")
    public AuthResponse login(
            @RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        AuthResponse authResponse = authService.login(request);
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, buildAuthCookie(authResponse, httpRequest).toString());
        return authResponse;
    }

    // ?area=admin|farmer|guest identifies which of the (possibly several,
    // one per role) cookies present in this browser to clear -- the caller
    // always knows its own area (it's the same prefix its ApiClient
    // instance was created with). Falls back to clearing all three when
    // area is missing/unrecognized so old clients don't leave a stale
    // cookie behind.
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @RequestParam(required = false) String area,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String targeted = JwtService.cookieNameForArea(area);
        List<String> cookieNames = targeted != null ? List.of(targeted) : JwtService.ALL_COOKIE_NAMES;
        for (String cookieName : cookieNames) {
            httpResponse.addHeader(HttpHeaders.SET_COOKIE, buildAuthCookie(cookieName, "", 0, httpRequest).toString());
        }
    }

    private ResponseCookie buildAuthCookie(AuthResponse authResponse, HttpServletRequest request) {
        String cookieName = JwtService.cookieNameFor(authResponse.role(), authResponse.accountType());
        return buildAuthCookie(cookieName, authResponse.accessToken(), authResponse.expiresInSeconds(), request);
    }

    private ResponseCookie buildAuthCookie(
            String cookieName, String token, long maxAgeSeconds, HttpServletRequest request) {
        return ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(request.isSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }
}
