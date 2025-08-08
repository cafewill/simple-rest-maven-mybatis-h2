package com.cube.simple.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.cube.simple.interceptor.SimpleInterceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * WebMvc 설정
 *
 * - SimpleInterceptor 를 /api/** 경로에 적용하고, 문서/정적/민감 경로는 제외한다.
 * - Spring Boot 3.x 는 기본 PathPatternParser 기반 매칭이므로 "/**" 와 같은 패턴 사용 가능.
 *
 * ⚠️ 왜 exclude 가 필요한가?
 *   - Swagger/H2/정적 리소스는 인터셉터 로깅 대상에서 제외하여 불필요한 로그를 줄이고,
 *     일부 페이지(프레임, 문서 JSON)를 가로채 생기는 부작용을 방지.
 *   - 로그인(/api/auth/login)은 민감 정보(자격 증명)가 포함될 수 있어 로깅 제외 권장.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SimpleConfig implements WebMvcConfigurer {

    private final SimpleInterceptor simpleInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        log.info("Check : SimpleConfig.addInterceptors()");

        registry.addInterceptor(simpleInterceptor)
            // 인터셉터 적용 대상
            .addPathPatterns("/api/**")

            // 문서/개발 도구 경로 (springdoc-openapi 기준)
            .excludePathPatterns("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")

            // H2 콘솔(로컬 개발 용)
            .excludePathPatterns("/h2-console/**")

            // 오류/헬스/정적 리소스
            .excludePathPatterns("/error", "/favicon.ico", "/webjars/**", "/resources/**", "/static/**", "/public/**")

            // 인증 엔드포인트(민감 페이로드 노출 방지)
            .excludePathPatterns("/api/auth/login")

            // 인터셉터 체인 내 실행 우선순위(여러 인터셉터가 있을 때 낮을수록 먼저 실행)
            .order(0);
    }
}
