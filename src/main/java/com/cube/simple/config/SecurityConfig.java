package com.cube.simple.config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // @PreAuthorize 등
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; // 웹 보안 활성화
import org.springframework.security.config.http.SessionCreationPolicy; // Stateless
import org.springframework.security.web.SecurityFilterChain; // 최신 진입점
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration; // CORS 설정
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.cube.simple.enums.RoleCode;
import com.cube.simple.filter.JwtAuthenticationFilter; // JWT 검증 필터
import com.cube.simple.handler.SimpleAccessDeniedHandler; // 403
import com.cube.simple.handler.SimpleAuthenticationEntryPoint; // 401
import com.cube.simple.util.JWTUtil;
import com.cube.simple.util.MessageUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JWTUtil jwtUtil;
    private final ObjectMapper objectMapper;
    
    private final MessageUtil messages;

    // --- CORS 프로퍼티 (환경별로 유연하게 관리) ---
    @Value("${cors.allowed-origins:http://localhost:8080}")
    private String allowedOrigins;
    @Value("${cors.allowed-methods:GET,POST,PUT,PATCH,DELETE,OPTIONS}")
    private String allowedMethods;
    @Value("${cors.allowed-headers:Authorization,Content-Type,X-Requested-With}")
    private String allowedHeaders;
    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;
    @Value("${cors.max-age:3600}")
    private long maxAgeSeconds;

    /**
     * 최신 Spring Security 6 방식의 보안 체인 구성
     * - CORS: 프리플라이트 허용 + 프로퍼티 기반 허용 목록
     * - CSRF: JWT/Stateless이므로 전역 비활성화
     * - 세션: STATELESS
     * - 인가 규칙: 메서드/경로별 명시
     * - 필터: JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 배치
     */
    @Bean
    @SuppressWarnings("squid:S4502") // SonarCloud CSRF 관련 규칙(S4502) 경고 억제 (의도적으로 특정 경로만 CSRF 예외 처리함)
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            SimpleAuthenticationEntryPoint authEntryPoint,
            SimpleAccessDeniedHandler accessDeniedHandler) throws Exception {

        log.info("Check : SecurityConfig.filterChain()");

        http
            // CORS 활성화 (아래 Bean 사용)
            .cors(Customizer.withDefaults())

            // CSRF 비활성화 (JWT + 헤더 기반 인증 → 쿠키 자동전송 X → CSRF 표적 아님)
            .csrf(csrf -> csrf.disable())

            // H2 콘솔 같은 iframe 필요 경로를 동일 출처만 허용
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

            // 무상태: 세션 저장하지 않음
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 401/403 핸들러
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authEntryPoint)
                .accessDeniedHandler(accessDeniedHandler))

            // 인가 규칙
            .authorizeHttpRequests(auth -> auth
                // CORS 프리플라이트 허용
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // 개발/문서 공개
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()

                // 퍼블릭 페이지
                .requestMatchers(HttpMethod.GET, "/", "/welcome").permitAll()

                // 로그인 공개
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

                // 데모 API: 조회 공개, 쓰기/수정/삭제는 ADMIN
                .requestMatchers(HttpMethod.GET,  "/api/demos", "/api/demos/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/demos").hasRole(RoleCode.ADMIN.name())
                .requestMatchers(HttpMethod.PUT,  "/api/demos/**").hasRole(RoleCode.ADMIN.name())
                .requestMatchers(HttpMethod.DELETE,"/api/demos/**").hasRole(RoleCode.ADMIN.name())

                // 아이템 API: 조회는 USER/ADMIN, 쓰기/수정/삭제는 ADMIN
                .requestMatchers(HttpMethod.GET,  "/api/items", "/api/items/**").hasAnyRole(RoleCode.USER.name(),RoleCode.ADMIN.name())
                .requestMatchers(HttpMethod.POST, "/api/items").hasRole(RoleCode.ADMIN.name())
                .requestMatchers(HttpMethod.PUT,  "/api/items/**").hasRole(RoleCode.ADMIN.name())
                .requestMatchers(HttpMethod.DELETE,"/api/items/**").hasRole(RoleCode.ADMIN.name())

                // 멤버 API (세부 본인확인은 @PreAuthorize에서 처리)
                // - POST /api/members       : ADMIN만(아래 @PreAuthorize와 일치)
                // - GET  /api/members       : ADMIN만(아래 @PreAuthorize와 일치)
                // - PUT/DELETE /api/members/{id} :
                //   여기서는 anyRequest().authenticated()로만 통과시켜 '본인 여부' 검증을
                //   MemberController 의 @PreAuthorize("hasRole('ADMIN') or #id == authentication.name")가 담당하게 함.
                .requestMatchers(HttpMethod.GET,  "/api/members").hasRole(RoleCode.ADMIN.name())
                .requestMatchers(HttpMethod.POST, "/api/members").hasRole(RoleCode.ADMIN.name())

                // 그 외는 인증 필요
                .anyRequest().authenticated()
            )

            // JWT 필터 등록: UsernamePasswordAuthenticationFilter 이전
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtUtil, objectMapper, messages),
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    /**
     * CORS 설정 Bean
     * - 허용 Origin/Method/Header를 프로퍼티로 제어
     * - allowCredentials=true인 경우, 와일드카드 Origin("*")은 사용할 수 없으므로 주의
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        List<String> origins = splitAndTrim(allowedOrigins);
        if (origins.size() == 1 && "*".equals(origins.get(0))) {
            // 자격 증명(쿠키/인증정보) 허용이 필요 없다면 패턴으로 전체 허용 가능
            config.addAllowedOriginPattern("*");
            config.setAllowCredentials(false);
        } else {
            config.setAllowedOrigins(origins);
            config.setAllowCredentials(allowCredentials);
        }

        config.setAllowedMethods(splitAndTrim(allowedMethods));
        config.setAllowedHeaders(splitAndTrim(allowedHeaders));
        config.setMaxAge(maxAgeSeconds);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private static List<String> splitAndTrim(String csv) {
        return Arrays.stream(csv.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }
}
