package com.poultry.broiler_farming_system.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (jwtService.isValid(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            String username = jwtService.extractUsername(token);
            // Re-resolved from the DB on every request (not just decoded from
            // the token) so a user banned mid-session loses access
            // immediately instead of waiting out the token's expiry.
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (userDetails.isEnabled()) {
                var authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER_NAME);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }

        // Page routes (JSP shells) are loaded by the browser's normal
        // navigation, not a fetch() call, so they can't attach an
        // Authorization header -- they authenticate via the role-specific
        // cookie set on login/register instead (admin_auth_token /
        // farmer_auth_token / guest_auth_token). Never honored for /api/**
        // so a forged cross-site request riding on a cookie alone still
        // can't reach the JSON API; every state-changing call must keep
        // presenting the header explicitly.
        if (request.getRequestURI().startsWith("/api") || request.getCookies() == null) {
            return null;
        }
        // The URL prefix pins which cookie is authoritative for this
        // request, so a Farmer's cookie can never be read while serving
        // /guest/** (and vice versa) even if both happen to be present in
        // the same browser at once.
        String cookieName = resolveCookieNameForPath(request.getRequestURI());
        if (cookieName == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String resolveCookieNameForPath(String uri) {
        if (uri.startsWith("/admin")) {
            return JwtService.ADMIN_COOKIE_NAME;
        }
        if (uri.startsWith("/farmer")) {
            return JwtService.FARMER_COOKIE_NAME;
        }
        if (uri.startsWith("/guest")) {
            return JwtService.GUEST_COOKIE_NAME;
        }
        return null;
    }
}
