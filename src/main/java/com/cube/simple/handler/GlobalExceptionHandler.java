package com.cube.simple.handler;

import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.cube.simple.dto.CommonResponse;
import com.cube.simple.enums.ResponseCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Value("${common.error.data:false}")
    private boolean showErrorData;

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    private String getMessage(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

    private String getMessage(String code, String defaultMessage) {
        return messageSource.getMessage(code, null, defaultMessage, LocaleContextHolder.getLocale());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex) {

        String detail = ex.getLocalizedMessage();
        
        log.info ("Unhandled exception occurred : {} {}", detail);
        
        CommonResponse<String> body = CommonResponse.<String>builder()
                .code(ResponseCode.INTERNAL_SERVER_ERROR)
                .message(getMessage("api.response.error", "Server error occurred."))
                .data(showErrorData ? detail : null)
                .build();

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // @Valid 검증 실패 시 (시그니처 변경: HttpStatus -> HttpStatusCode)
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        String detail = ex.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        
        log.info("Validation failed : {}", detail);

        CommonResponse<String> body = CommonResponse.<String>builder()
                .code(ResponseCode.BAD_REQUEST)
                .message(getMessage("api.response.bad.request", "Invalid request parameter."))
                .data(showErrorData ? detail : null)
                .build();

        return new ResponseEntity<>(body, headers, status);
    }

    // 파라미터 타입 불일치 
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    	
        String detail = String.format("파라미터 '%s'은(는) %s 타입이어야 합니다.",
                ex.getName(), Objects.toString(ex.getRequiredType(), "Unknown"));

        log.info("Type mismatch : {}", detail);

        CommonResponse<String> body = CommonResponse.<String>builder()
                .code(ResponseCode.BAD_REQUEST)
                .message(getMessage("api.response.bad.request", "Invalid request parameter."))
                .data(showErrorData ? detail : null)
                .build();

        return ResponseEntity.status(ResponseCode.BAD_REQUEST.getHttpStatus()).body(body);
    }

    // 인증 실패 (401)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthentication(AuthenticationException ex) {
    	
        String detail = ex.getLocalizedMessage ();

        log.info("Unauthorized : {}", detail);

        CommonResponse<String> body = CommonResponse.<String>builder()
                .code(ResponseCode.UNAUTHORIZED)
                .message(getMessage("api.response.unauthorized", "Authentication failed."))
                .data(showErrorData ? detail : null)
                .build();

        return ResponseEntity.status(ResponseCode.UNAUTHORIZED.getHttpStatus()).body(body);
    }

    // 권한 부족 (403)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
    	
        String detail = ex.getLocalizedMessage ();
        
        log.info("Forbidden : {}", detail);

        CommonResponse<String> body = CommonResponse.<String>builder()
                .code(ResponseCode.FORBIDDEN)
                .message(getMessage("api.response.forbidden", "Access is denied."))
                .data(showErrorData ? detail : null)
                .build();

        return ResponseEntity.status(ResponseCode.FORBIDDEN.getHttpStatus()).body(body);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {

        String detail = body instanceof String ? (String) body : ex.getLocalizedMessage();

        log.error("Internal server error occurred : {}", detail);

        CommonResponse<String> commonResponse = CommonResponse.<String>builder()
                .code(ResponseCode.of(statusCode))
                .message(getMessage("api.response.error", "Server error occurred."))
                .data(showErrorData ? Objects.toString(detail, "Server error occurred.") : null)
                .build();

        return new ResponseEntity<>(commonResponse, headers, statusCode);
    }
}