package com.poultry.broiler_farming_system.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// For the server-rendered /admin/**, /farmer/** and /guest/** shells, as
// opposed to the JSON /api/** surface (handled by RestAuthenticationEntryPoint):
// an unauthenticated visitor is bounced to the matching login page instead
// of getting a bare 401 with no body.
@Component
public class PageAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        response.sendRedirect(loginPageFor(request));
    }

    static String loginPageFor(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/admin") ? "/admin/login" : "/login";
    }
}
