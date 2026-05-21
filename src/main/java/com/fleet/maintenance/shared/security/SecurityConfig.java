package com.fleet.maintenance.shared.security;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final List<String> allowedOrigins;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            @Value("${security.cors.allowed-origins:http://localhost:3000,http://localhost:4200}") String allowedOrigins
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/*.js","/*.css","/*.ico","/*.html","/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login","/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/dashboard", "/api/dashboard").hasAnyAuthority("ROLE_COORDINATOR", "ROLE_PROVIDER")
                        .requestMatchers(HttpMethod.POST, "/maintenance-requests", "/api/maintenance-requests").hasAuthority("ROLE_COORDINATOR")
                        .requestMatchers(HttpMethod.PUT, "/maintenance-requests/*/assign-provider", "/api/maintenance-requests/*/assign-provider").hasAuthority("ROLE_COORDINATOR")
                        .requestMatchers(HttpMethod.POST, "/maintenance-requests/*/inspection", "/api/maintenance-requests/*/inspection").hasAuthority("ROLE_PROVIDER")
                        .requestMatchers(HttpMethod.POST, "/maintenance-requests/*/decision", "/api/maintenance-requests/*/decision").hasAuthority("ROLE_COORDINATOR")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
