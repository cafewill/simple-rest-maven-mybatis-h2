package com.cube.simple.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import com.cube.simple.filter.JwtAuthenticationFilter;
import com.cube.simple.handler.SimpleAccessDeniedHandler;
import com.cube.simple.handler.SimpleAuthenticationEntryPoint;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    // @Autowired
    // private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
            SimpleAuthenticationEntryPoint authEntryPoint,
            SimpleAccessDeniedHandler accessDeniedHandler) throws Exception {
    
        log.info ("Check : SecurityConfig.filterChain ()");

        http
        .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/h2-console/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"))
                // .disable())
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authEntryPoint)
                .accessDeniedHandler(accessDeniedHandler))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**").permitAll()
        	    .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                .requestMatchers(HttpMethod.GET,	"/").permitAll()
                .requestMatchers(HttpMethod.GET,	"/welcome").permitAll()
                .requestMatchers(HttpMethod.POST,	"/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET,	"/api/demos").permitAll()
                .requestMatchers(HttpMethod.GET,	"/api/demos/**").permitAll()
                .requestMatchers(HttpMethod.PUT,	"/api/demos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST,	"/api/demos").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET,	"/api/items").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.GET,	"/api/items/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.PUT,	"/api/items/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST,	"/api/items").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET,	"/api/members").hasAnyRole("ADMIN")
                .requestMatchers(HttpMethod.POST,	"/api/members").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
