//package com.letmeknow.config.auth;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.ProviderManager;
//import com.letmeknow.config.security.CustomAuthenticationProvider;
//
//@Configuration
//@RequiredArgsConstructor
//public class AuthConfig {
//    private final CustomAuthenticationProvider customAuthenticationProvider;
//
//    @Bean
//    public AuthenticationManager authenticationManager() {
//        return new ProviderManager(customAuthenticationProvider);
//    }
//}
