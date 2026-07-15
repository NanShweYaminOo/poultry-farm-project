package com.poultry.broiler_farming_system.security;

import com.poultry.broiler_farming_system.entity.enums.AccountType;
import com.poultry.broiler_farming_system.entity.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
public class JwtService {

    // Same JWT the API accepts via the Authorization header, also handed
    // out as an HttpOnly cookie so the server-rendered /admin/**,
    // /farmer/** and /guest/** JSP shells (which can't attach a Bearer
    // header) can be authenticated on page load. See JwtAuthenticationFilter
    // and AuthController.
    //
    // One cookie name per area rather than one shared name -- a shared
    // cookie meant a Farmer login in one tab silently overwrote a Guest
    // login in another tab of the same browser (and vice versa), since
    // cookies are scoped to the whole origin, not to a tab. Separate names
    // let independent role sessions coexist in the same browser.
    public static final String ADMIN_COOKIE_NAME = "admin_auth_token";
    public static final String FARMER_COOKIE_NAME = "farmer_auth_token";
    public static final String GUEST_COOKIE_NAME = "guest_auth_token";
    public static final List<String> ALL_COOKIE_NAMES =
            List.of(ADMIN_COOKIE_NAME, FARMER_COOKIE_NAME, GUEST_COOKIE_NAME);

    public static String cookieNameFor(UserRole role, AccountType accountType) {
        if (role == UserRole.ADMIN) {
            return ADMIN_COOKIE_NAME;
        }
        return accountType == AccountType.FARMER ? FARMER_COOKIE_NAME : GUEST_COOKIE_NAME;
    }

    // "admin" / "farmer" / "guest" -- matches the ApiClient prefix each
    // area's common.js is created with, so the client can tell the server
    // exactly which of its (possibly several, one per role) cookies to
    // clear on logout without guessing.
    public static String cookieNameForArea(String area) {
        if ("admin".equals(area)) {
            return ADMIN_COOKIE_NAME;
        }
        if ("farmer".equals(area)) {
            return FARMER_COOKIE_NAME;
        }
        if ("guest".equals(area)) {
            return GUEST_COOKIE_NAME;
        }
        return null;
    }

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(UserPrincipal principal) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(principal.getUsername())
                .claim("userId", principal.getId())
                .claim("role", principal.getUser().getRole().name())
                .claim("accountType", principal.getUser().getAccountType().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(key)
                .compact();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
