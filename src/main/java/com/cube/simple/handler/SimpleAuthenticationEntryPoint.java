package com.cube.simple.handler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.cube.simple.dto.CommonResponse;
import com.cube.simple.enums.ResponseCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimpleAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Value("${common.error.data:false}")
    private boolean showErrorData;

    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException ex) throws IOException {

    	String detail = String.format ("%s %s", "Unauthorized access", Objects.requireNonNullElse(ex.getMessage(), ""));
        log.info(detail);

        String message = messageSource.getMessage(
                "api.response.unauthorized",
                null,
                "Authentication failed.",
                LocaleContextHolder.getLocale()
            );

        CommonResponse<String> body = CommonResponse.<String>builder()
                .code(ResponseCode.UNAUTHORIZED)
                .message(message)
                .data(showErrorData ? detail : null)
                .build();

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(ResponseCode.UNAUTHORIZED.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }
}