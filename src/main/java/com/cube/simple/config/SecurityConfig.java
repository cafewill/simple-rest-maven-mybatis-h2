package com.cube.simple.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.cube.simple.security.jwt.JwtAuthenticationFilter;
import com.cube.simple.security.jwt.JwtUtil;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화
            .csrf(csrf -> csrf.disable())
            // Stateless 세션 설정
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 인증/인가 규칙
            .authorizeHttpRequests(auth -> auth
        	    .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/demos").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/items").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/items/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/members").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/members/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/demos/**", "/api/items/**", "/api/members/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            // JWT 필터 추가
            .addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}