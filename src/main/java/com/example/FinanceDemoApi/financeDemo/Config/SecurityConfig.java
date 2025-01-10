package com.example.FinanceDemoApi.financeDemo.Config;

import com.example.FinanceDemoApi.financeDemo.Filters.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // whitelist of endpoints without authorization
    private static final List<String> WHITELIST = Arrays.asList(
            "/v1/auth/google",
            "/v1/test/test3",
            "/v1/test/test4",
            "/v1/auth/register"

    );

    // WhiteList of the endpoints that are accessible to free users
    private static final List<String> FREE_USER_ENDPOINTS = Arrays.asList(
            "/v1/free/resource1",
            "/v1/free/resource2"
    );

    // WhiteList of the endpoints that are accessible to premium users
    private static final List<String> PREMIUM_USER_ENDPOINTS = Arrays.asList(
            "/v1/premium/resource1",
            "/v1/premium/resource2",
            "/v1/premium/resource3"
    );

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for APIs
                .authorizeHttpRequests(request -> request
                        .requestMatchers(WHITELIST.toArray(new String[0])).permitAll()  // endpoints available without authorization
                        .requestMatchers(FREE_USER_ENDPOINTS.toArray(new String[0])).hasAuthority("ROLE_free")
                        .requestMatchers(PREMIUM_USER_ENDPOINTS.toArray(new String[0])).hasAuthority("ROLE_premium")
                        .anyRequest().authenticated()  // All other endpoints require authentication
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

}
