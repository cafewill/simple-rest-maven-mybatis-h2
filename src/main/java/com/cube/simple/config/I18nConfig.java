package com.cube.simple.config;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

@Configuration
public class I18nConfig {

    // ===== 메시지 소스 =====
    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
        // NOTE: classpath 루트에 i18n/messages_xx.properties 파일이 있어야 합니다.
        ms.setBasename("i18n/messages");
        ms.setDefaultEncoding("UTF-8");
        ms.setFallbackToSystemLocale(false);
        ms.setCacheSeconds(10);
        return ms;
    }

    // ===== Locale 설정 (세션 + ?lang=) =====
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.KOREAN);
        return slr;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    @Bean
    public WebMvcConfigurer localeInterceptorConfig(LocaleChangeInterceptor lci) {
        return new WebMvcConfigurer() {
            @Override public void addInterceptors(InterceptorRegistry reg) {
                reg.addInterceptor(lci);
            }
        };
    }

    // ===== OpenAPI i18n 커스터마이저 (중첩 토큰 지원) =====
    private static final Pattern TOKEN =
        Pattern.compile("\\{\\s*([\\w\\.]+)\\s*(?:\\|\\s*([^{}]*?)\\s*)?\\}");
    private static final int MAX_DEPTH = 5; // 무한루프 방지

    private static String unquote(String s) {
        if (s == null) return null;
        s = s.trim();
        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'")))
            return s.substring(1, s.length() - 1);
        return s;
    }
    private static boolean hasToken(String s) { return s != null && TOKEN.matcher(s).find(); }

    private static String resolveAll(MessageSource ms, String text, Locale locale) {
        if (text == null || text.isEmpty()) return text;
        String current = text;
        for (int depth = 0; depth < MAX_DEPTH && hasToken(current); depth++) {
            StringBuffer sb = new StringBuffer();
            Matcher m = TOKEN.matcher(current);
            while (m.find()) {
                String code = m.group(1);
                String argsStr = m.group(2);
                String[] args = (argsStr == null || argsStr.isBlank())
                                ? new String[0]
                                : java.util.Arrays.stream(argsStr.split("\\|"))
                                  .map(String::trim)
                                  .map(I18nConfig::unquote)
                                  .map(arg -> resolveAll(ms, arg, locale)) // ★ 인자도 재귀 치환
                                  .toArray(String[]::new);

                String replacement = ms.getMessage(code, args, code, locale);
                replacement = Matcher.quoteReplacement(replacement);
                m.appendReplacement(sb, replacement);
            }
            m.appendTail(sb);
            current = sb.toString();
        }
        return current;
    }

    @Bean
    public OpenApiCustomizer openApiI18nCustomizer(MessageSource messageSource) {
        return openApi -> {
            Locale locale = LocaleContextHolder.getLocale();

            // ================================================================
            //      ADDED: 태그(Tag) 이름 다국어 처리 로직 추가
            // ================================================================
            if (openApi.getTags() != null) {
                openApi.getTags().forEach(tag ->
                    tag.setName(resolveAll(messageSource, tag.getName(), locale))
                );
            }

            // (기존 코드) Paths 및 Operations 다국어 처리
            if (openApi.getPaths() != null) {
                openApi.getPaths().forEach((path, item) ->
                    item.readOperations().forEach(op -> {
                        op.setSummary(    resolveAll(messageSource, op.getSummary(),     locale));
                        op.setDescription(resolveAll(messageSource, op.getDescription(), locale));

                        if (op.getResponses() != null) {
                            op.getResponses().forEach((code, resp) ->
                                resp.setDescription(resolveAll(messageSource, resp.getDescription(), locale)));
                        }
                        if (op.getParameters() != null) {
                            op.getParameters().forEach(param ->
                                param.setDescription(resolveAll(messageSource, param.getDescription(), locale)));
                        }
                        if (op.getRequestBody() != null && op.getRequestBody().getDescription() != null) {
                            op.getRequestBody().setDescription(
                                resolveAll(messageSource, op.getRequestBody().getDescription(), locale));
                        }
                    })
                );
            }

            // (기존 코드) API 정보(Info) 다국어 처리
            if (openApi.getInfo() != null) {
                openApi.getInfo().setTitle(
                    resolveAll(messageSource, openApi.getInfo().getTitle(), locale));
                openApi.getInfo().setDescription(
                    resolveAll(messageSource, openApi.getInfo().getDescription(), locale));
            }
        };
    }
}