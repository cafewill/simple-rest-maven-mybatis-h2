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

@Slf4j
@Configuration
public class SwaggerConfig {

    private final MessageSource messageSource;

    public SwaggerConfig(MessageSource messageSource) {
        this.messageSource = Objects.requireNonNull(messageSource);
    }

    @Bean
    public OpenAPI openAPI() {
    	
        Locale locale = LocaleContextHolder.getLocale();

        log.info ("Check : SwaggerConfig.openAPI (), local={}", locale);

        String title = messageSource.getMessage("swagger.title", null, locale);
        String version = messageSource.getMessage("swagger.version", null, locale);
        String desc = messageSource.getMessage("swagger.description", null, locale);
        String contactName = messageSource.getMessage("swagger.contact.name", null, locale);
        String contactUrl = messageSource.getMessage("swagger.contact.url", null, locale);

        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .version(version)
                        .description(desc)
                        .contact(new Contact()
                            .name(contactName)
                            .url(contactUrl)
                        )
                    )        		
            .addSecurityItem(new SecurityRequirement().addList("JWT"))
            .components(new Components()
                .addSecuritySchemes("JWT", 
                    new SecurityScheme()
                        .name("Authorization")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(SecurityScheme.In.HEADER)
                )
            );
    }
    
    private static final Pattern MSG_PATTERN = Pattern.compile("\\{(.+?)\\}");

    private String resolveMessageKeys(String text, MessageSource ms, Locale locale) {
        if (text == null) return null;
        Matcher matcher = MSG_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            String msg = ms.getMessage(key, null, key, locale);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(msg));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    @Bean
    public OpenApiCustomizer localisedOpenApi(MessageSource messageSource) {
        return openApi -> {
            Locale locale = LocaleContextHolder.getLocale();

            // Summary, Description, Responses 모두 순회하며 {key} 치환
            openApi.getPaths().values()
                .forEach(pathItem ->
                    pathItem.readOperations().forEach(op -> {
                        op.setSummary(resolveMessageKeys(op.getSummary(), messageSource, locale));
                        op.setDescription(resolveMessageKeys(op.getDescription(), messageSource, locale));
                        if (op.getResponses() != null) {
                            op.getResponses().values().forEach(r ->
                                r.setDescription(resolveMessageKeys(r.getDescription(), messageSource, locale))
                            );
                        }
                    })
                );
        };
    }    
}
