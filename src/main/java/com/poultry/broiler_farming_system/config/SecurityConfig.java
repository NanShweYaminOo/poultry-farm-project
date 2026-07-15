package com.poultry.broiler_farming_system.config;

import com.poultry.broiler_farming_system.security.JwtAuthenticationFilter;
import com.poultry.broiler_farming_system.security.PageAccessDeniedHandler;
import com.poultry.broiler_farming_system.security.PageAuthenticationEntryPoint;
import com.poultry.broiler_farming_system.security.RestAccessDeniedHandler;
import com.poultry.broiler_farming_system.security.RestAuthenticationEntryPoint;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;
    private final PageAuthenticationEntryPoint pageAuthenticationEntryPoint;
    private final PageAccessDeniedHandler pageAccessDeniedHandler;
    private final UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .exceptionHandling(eh -> eh
                        // AntPathRequestMatcher အစား request URI ကို တိုက်ရိုက်စစ်ဆေးပြီး Error ကို ကျော်လွှားခြင်း
                        // /api/** gets the JSON 401/403 body; every other path (the
                        // server-rendered /admin/**, /farmer/** and /guest/** JSP
                        // shells) gets bounced to the matching login page instead.
                        .defaultAuthenticationEntryPointFor(restAuthenticationEntryPoint,
                                request -> request.getRequestURI().startsWith("/api"))
                        .defaultAuthenticationEntryPointFor(pageAuthenticationEntryPoint,
                                request -> !request.getRequestURI().startsWith("/api"))
                        .defaultAccessDeniedHandlerFor(restAccessDeniedHandler,
                                request -> request.getRequestURI().startsWith("/api"))
                        .defaultAccessDeniedHandlerFor(pageAccessDeniedHandler,
                                request -> !request.getRequestURI().startsWith("/api")))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.INCLUDE).permitAll()
                        .requestMatchers("/admin/login", "/login", "/register", "/public/assets/**", "/farmer/assets/**", "/guest/assets/**", "/shared/assets/**", "/css/**", "/js/**", "/images/**", "/uploads/**", "/WEB-INF/jsp/**").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // The STOMP handshake itself (GET /ws, or SockJS's info/xhr
                        // negotiation requests under /ws/**) is intentionally public --
                        // it carries no JWT the servlet filter chain could check (see
                        // StompAuthChannelInterceptor's Javadoc for why). Real
                        // authentication happens per STOMP session on the CONNECT
                        // frame instead, which this HTTP-level rule cannot see or
                        // enforce.
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/payment-transactions/*/review").hasRole("ADMIN")
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/reports/admin-audit").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/breeds").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/breeds/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/breeds/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/diseases").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/diseases/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/diseases/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/faqs").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/faqs/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/faqs/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/knowledge-posts").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/knowledge-posts/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/knowledge-posts/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/batches/*/start", "/api/v1/batches/*/stop").hasAnyRole("PAID", "ADMIN")
                        .requestMatchers("/api/v1/daily-logs/**").hasAnyRole("PAID", "ADMIN")
                        .requestMatchers("/api/v1/batch-alarms/**").hasAnyRole("PAID", "ADMIN")
                        .requestMatchers("/api/v1/medicine-estimation/**").hasAnyRole("PAID", "ADMIN")
                        .requestMatchers("/api/v1/inventory/**").hasAnyRole("PAID", "ADMIN")
                        .requestMatchers("/api/v1/group-chats/**").hasAnyRole("PAID", "ADMIN")
                        .requestMatchers("/api/v1/chatbot/**").hasAnyRole("PAID", "ADMIN")
                        // Buy requests are free for any authenticated account -- FREE
                        // or PAID, GUEST or FARMER -- so intentionally no role
                        // restriction here (falls through to anyRequest().authenticated()
                        // below); the previous hasAnyRole("FARMER","ADMIN") wrongly
                        // blocked GUEST accountType users, since ROLE_FARMER only comes
                        // from AccountType.FARMER, not from UserRole.
                        //
                        // The JSP page shells: previously permitAll(), so anyone could
                        // load them even though the data APIs behind them were already
                        // protected. /admin/login stays public above; every other admin
                        // page now requires ROLE_ADMIN.
                        //
                        // Farmer and Guest used to share one "/dashboard/**" area,
                        // distinguished only by client-side JS reading the accountType
                        // field -- nothing server-side stopped a Guest token from
                        // rendering Farmer-only pages. Splitting the URL space by role
                        // here (backed by the ROLE_FARMER/ROLE_GUEST authorities
                        // UserPrincipal already grants) means the server refuses to
                        // serve the wrong area's pages even if client-side state is
                        // stale or tampered with.
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/farmer/**").hasRole("FARMER")
                        .requestMatchers("/guest/**").hasRole("GUEST")
                        .anyRequest().authenticated()
                )
//                .formLogin(form -> form
//                        .loginPage("/admin/login")
//                        .permitAll()
//                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/css/**",
                "/js/**",
                "/images/**",
                "/admin/assets/**",
                "/public/assets/**",
                "/farmer/assets/**",
                "/guest/assets/**",
                "/shared/assets/**",
                "/WEB-INF/jsp/**"
        );
    }
}