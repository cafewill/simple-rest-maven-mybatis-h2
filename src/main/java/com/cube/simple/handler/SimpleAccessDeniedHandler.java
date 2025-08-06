package com.cube.simple.handler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
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
public class SimpleAccessDeniedHandler implements AccessDeniedHandler {

    @Value("${common.error.data:false}")
    private boolean showErrorData;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {

    	String detail = String.format ("%s %s", "Forbidden access", Objects.requireNonNullElse(ex.getMessage(), ""));
        log.info(detail);

        CommonResponse<String> body = CommonResponse.<String>builder()
                .code(ResponseCode.FORBIDDEN)
                .message(ResponseCode.FORBIDDEN.getMessage())
                .data(showErrorData ? detail : null)
                .build();

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(ResponseCode.UNAUTHORIZED.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }
}