package com.example.demo.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private static final String[] AUTH_WHITELIST = {
            "/auth/**" // Allow unrestricted access to the auth endpoints
    };

    @Value("${client-jwk-set-uri}")
    private String clientJwtSetUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(request -> new CorsConfiguration(corsFilter())))
                .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless APIs
                .authorizeHttpRequests(req -> req
                        .requestMatchers(AUTH_WHITELIST).permitAll() // Allow auth endpoint
                        .anyRequest().authenticated() // Protect all other endpoints
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless session
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationManagerResolver(authenticationManagerResolver(jwtClientDecoder()))

                )
                .build();
    }

    @Bean
    public SimpleAuthorityMapper keycloakAuthorityMapper() {
        SimpleAuthorityMapper authorityMapper = new SimpleAuthorityMapper();
        authorityMapper.setConvertToUpperCase(true);
        return authorityMapper;
    }

    @Bean
    public AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver(JwtDecoder jwtClientDecoder) {
        JwtAuthenticationProvider jwtClientAuthenticationProvider = new JwtAuthenticationProvider(jwtClientDecoder);

        AuthenticationManager clientAuthManager = new ProviderManager(jwtClientAuthenticationProvider);
        return request -> clientAuthManager;
    }

    @Bean
    public JwtDecoder jwtClientDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(clientJwtSetUri).build();
    }

    private CorsConfiguration corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:3000"); // Adjust allowed origins as needed
        config.addAllowedOrigin("http://38.242.218.13:8096");
        config.addAllowedHeader("*");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        return config;
    }
}
