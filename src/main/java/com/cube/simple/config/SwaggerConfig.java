package com.cube.simple.config;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;

/**
 * Swagger / OpenAPI 설정 (springdoc-openapi)
 *
 * 기능 요약
 * 1) i18n MessageSource 기반으로 문서 메타데이터(제목/버전/설명/연락처)를 현재 Locale에 맞게 로드
 * 2) 전역 보안 스킴에 JWT Bearer 등록(Authorization 헤더)
 * 3) OpenApiCustomizer 로 각 Operation 의 summary/description/response description 안의 {msg.key} 를
 *    메시지 번들 값으로 치환하여 다국어 문서화 지원
 *
 * 메시지 프로퍼티 예시
 * - i18n/messages_ko.properties
 *   swagger.title=내 API 문서
 *   swagger.version=v1
 *   swagger.description=설명입니다
 *   swagger.contact.name=담당자
 *   swagger.contact.url=https://example.com
 */
@Slf4j
@Configuration
public class SwaggerConfig {

    private final MessageSource messageSource;

    public SwaggerConfig(MessageSource messageSource) {
        // MessageSource 는 반드시 필요하므로 명시적으로 null 체크
        this.messageSource = Objects.requireNonNull(messageSource);
    }

    /**
     * OpenAPI 메타 정보 구성 + 전역 JWT 보안 스킴 등록
     */
    @Bean
    public OpenAPI openAPI() {
        // 요청 컨텍스트의 Locale(Interceptor/Resolver에 의해 결정됨)
        Locale locale = LocaleContextHolder.getLocale();

        log.info("Check : SwaggerConfig.openAPI(), locale={}", locale);

        // i18n 메시지 로드(키가 없으면 NoSuchMessageException 발생)
        String title       = messageSource.getMessage("swagger.title",          null, locale);
        String version     = messageSource.getMessage("swagger.version",        null, locale);
        String description = messageSource.getMessage("swagger.description",    null, locale);
        String contactName = messageSource.getMessage("swagger.contact.name",   null, locale);
        String contactUrl  = messageSource.getMessage("swagger.contact.url",    null, locale);

        // 문서 정보 + JWT 보안 스킴 추가
        return new OpenAPI()
            .info(new Info()
                .title(title)
                .version(version)
                .description(description)
                .contact(new Contact()
                    .name(contactName)
                    .url(contactUrl)
                )
            )
            // 스키마 이름 "JWT" 를 전역 요구 보안으로 등록 -> 각 Operation에 토큰 필요
            .addSecurityItem(new SecurityRequirement().addList("JWT"))
            .components(new Components()
                .addSecuritySchemes("JWT",
                    new SecurityScheme()
                        .name("Authorization")           // 헤더 이름
                        .type(SecurityScheme.Type.HTTP)  // HTTP 인증
                        .scheme("bearer")                // Bearer 스킴
                        .bearerFormat("JWT")             // 포맷 힌트
                        .in(SecurityScheme.In.HEADER)    // 헤더에서 읽음
                )
            );
    }

    /**
     * {message.key} 형태를 찾아 치환하기 위한 정규식
     *  - non-greedy(최소 매칭)로 중괄호 내부 키만 추출
     *  - 예) "api.member.insert.summary={api.member.insert.summary}"
     */
    private static final Pattern MSG_PATTERN = Pattern.compile("\\{(.+?)\\}");

    /**
     * 주어진 문자열 내 {key} 들을 MessageSource 값으로 치환
     *  - key 가 없을 때는 기본값으로 key 자체를 사용(문서가 비어 보이지 않도록)
     *  - null 입력 시 null 반환하여 원래 값 보존
     */
    private String resolveMessageKeys(String text, MessageSource ms, Locale locale) {
        if (Objects.isNull(text)) return null;

        Matcher matcher = MSG_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            // 키가 없을 경우에는 기본값으로 key 를 그대로 사용
            String msg = ms.getMessage(key, null, key, locale);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(msg));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Operation 수준의 summary/description 및 response description 을
     * 현재 Locale 과 MessageSource 기반으로 치환해 주는 커스터마이저
     *
     * 주의
     * - openApi.getPaths() 가 null 일 수 있으므로 NPE 가드
     * - 필요 시 parameters/requestBody 등으로 확장 가능(아래 TODO 참고)
     */
    @Bean
    public OpenApiCustomizer localisedOpenApi(MessageSource messageSource) {
        return openApi -> {
            Locale locale = LocaleContextHolder.getLocale();

            if (openApi.getPaths() == null) return; // NPE 방지

            openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(op -> {
                    // Operation summary/description 치환
                    op.setSummary(resolveMessageKeys(op.getSummary(), messageSource, locale));
                    op.setDescription(resolveMessageKeys(op.getDescription(), messageSource, locale));

                    // Response description 치환
                    if (op.getResponses() != null) {
                        op.getResponses().values().forEach(r ->
                            r.setDescription(resolveMessageKeys(r.getDescription(), messageSource, locale))
                        );
                    }

                    // TODO: 필요 시 확장
                    // - 파라미터 설명: op.getParameters()
                    // - 요청 바디 설명: op.getRequestBody()
                    // - 스키마/예제 설명: openApi.getComponents().getSchemas() ...
                })
            );
        };
    }
}
