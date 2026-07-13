package com.poultry.broiler_farming_system.config;

import com.poultry.broiler_farming_system.security.JwtAuthenticationFilter;
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
                        .defaultAuthenticationEntryPointFor(restAuthenticationEntryPoint,
                                request -> request.getRequestURI().startsWith("/api"))
                        .accessDeniedHandler(restAccessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.INCLUDE).permitAll()
                        .requestMatchers("/admin/**", "/login", "/register", "/public/assets/**", "/css/**", "/js/**", "/images/**", "/uploads/**", "/WEB-INF/jsp/**").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/payment-transactions/*/review").hasRole("ADMIN")
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
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
                        .requestMatchers("/api/v1/batch-alarms/**").hasAnyRole("PAID", "ADMIN")
                        .requestMatchers("/api/v1/medicine-estimation/**").hasAnyRole("PAID", "ADMIN")
                        .requestMatchers("/api/v1/inventory/**").hasAnyRole("PAID", "ADMIN")
                        .requestMatchers("/api/v1/group-chats/**").hasAnyRole("PAID", "ADMIN")
                        .requestMatchers("/api/v1/chatbot/**").hasAnyRole("PAID", "ADMIN")
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
                "/WEB-INF/jsp/**"
        );
    }
}