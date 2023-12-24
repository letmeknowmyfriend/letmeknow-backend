package com.letmeknow.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {
    @Value("${cors.allowed-origin}")
    private String allowedOrigin;

    @Value("${port}")
    private String port;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); // Whether user credentials are supported.

        config.setAllowedHeaders(List.of("Authorization", "AuthorizationRefresh, DeviceToken"));
        config.setExposedHeaders(List.of("Authorization", "AuthorizationRefresh, DeviceToken"));

        config.setAllowedOrigins(List.of(allowedOrigin + ":" + port));
        config.setAllowedMethods(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
