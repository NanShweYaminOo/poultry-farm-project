package com.poultry.broiler_farming_system.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Mirrors PageAuthenticationEntryPoint for the "authenticated but wrong
// role" case. For a Farmer/Guest mismatch (e.g. a Guest account hitting
// /farmer/**) this sends the visitor back to their own area's home instead
// of a login page they're already past -- they're authenticated, just in
// the wrong place. Falls back to the matching login page for every other
// case (e.g. a non-ADMIN hitting /admin/**), same as before.
@Component
public class PageAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        String uri = request.getRequestURI();
        if (uri.startsWith("/farmer") || uri.startsWith("/guest")) {
            response.sendRedirect(ownAreaHomeFor(SecurityContextHolder.getContext().getAuthentication()));
            return;
        }
        response.sendRedirect(PageAuthenticationEntryPoint.loginPageFor(request));
    }

    private String ownAreaHomeFor(Authentication authentication) {
        if (authentication == null) {
            return "/login";
        }
        boolean isFarmer = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_FARMER"::equals);
        return isFarmer ? "/farmer" : "/guest";
    }
}
