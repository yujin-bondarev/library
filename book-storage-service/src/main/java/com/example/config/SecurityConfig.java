package com.example.config;

import com.example.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, @Value("${jwt.secret}") String secret) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeRequests(requests -> requests
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthFilter(secret), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}