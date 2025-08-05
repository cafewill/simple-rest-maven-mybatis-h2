package com.cube.simple.handler;

import java.util.Objects;
import java.util.stream.Collectors;

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex) {

        log.info ("Unhandled exception occurred: {}", ex.getMessage(), ex);

        CommonResponse<String> body = CommonResponse.<String>builder()
                .code(ResponseCode.INTERNAL_SERVER_ERROR)
                .message(ResponseCode.INTERNAL_SERVER_ERROR.getMessage())
                .data(ex.getMessage())
                .build();

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 1) @Valid 검증 실패 시 (시그니처 변경: HttpStatus -> HttpStatusCode)
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        String detail = ex.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        log.info("Validation failed: {}", detail);

        CommonResponse<String> body = CommonResponse.<String>builder()
                .code(ResponseCode.BAD_REQUEST)
                .message(ResponseCode.BAD_REQUEST.getMessage())
                .data(detail)
                .build();

        return new ResponseEntity<>(body, headers, status);
    }

    // 2) 파라미터 타입 불일치 (기존 코드 유지)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String detail = String.format("파라미터 '%s'은(는) %s 타입이어야 합니다.",
                ex.getName(), Objects.toString(ex.getRequiredType(), "Unknown"));

        log.info("Type mismatch: {}", detail);

        CommonResponse<String> body = CommonResponse.<String>builder()
                .code(ResponseCode.BAD_REQUEST)
                .message(ResponseCode.BAD_REQUEST.getMessage())
                .data(detail)
                .build();

        return ResponseEntity.status(ResponseCode.BAD_REQUEST.getHttpStatus()).body(body);
    }

    // 3) 존재하지 않는 URL 요청 (handleNoHandlerFoundException은 WebMvcConfigurationSupport#handleNoHandlerFoundException을 통해 호출되므로 @ExceptionHandler 제거)
    // NOTE: 이 핸들러가 동작하려면 application.yml(properties) 파일에 설정이 필요합니다. (하단 설명 참고)
    /*
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            ServletException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        String detail = String.format("'%s' 엔드포인트를 찾을 수 없습니다.", ex.getMessage());
        log.info("No handler found: {}", detail);

        CommonResponse<String> body = CommonResponse.<String>builder()
                .code(ResponseCode.NOT_FOUND)
                .message(ResponseCode.NOT_FOUND.getMessage())
                .data(detail)
                .build();

        return new ResponseEntity<>(body, headers, status);
    }
    */

    // 4) 인증 실패 (401)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthentication(AuthenticationException ex) {
    	
        log.info("Unauthorized: {}", ex.getMessage());

        CommonResponse<String> body = CommonResponse.<String>builder()
                .code(ResponseCode.UNAUTHORIZED)
                .message(ResponseCode.UNAUTHORIZED.getMessage())
                .data(ex.getMessage())
                .build();

        return ResponseEntity.status(ResponseCode.UNAUTHORIZED.getHttpStatus()).body(body);
    }

    // 5) 권한 부족 (403)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
    	
        log.info("Forbidden: {}", ex.getMessage());

        CommonResponse<String> body = CommonResponse.<String>builder()
                .code(ResponseCode.FORBIDDEN)
                .message(ResponseCode.FORBIDDEN.getMessage())
                .data(ex.getMessage())
                .build();

        return ResponseEntity.status(ResponseCode.FORBIDDEN.getHttpStatus()).body(body);
    }

    // 6) 그 밖의 모든 예외 처리 (REMOVED)
    // NOTE: 아래 handleExceptionInternal 메서드가 모든 예외의 최종 처리자 역할을 하므로
    //       @ExceptionHandler(Exception.class) 핸들러는 중복되어 제거합니다.
    //       이를 통해 예외 처리 로직을 일관성 있게 관리할 수 있습니다.

    // 7) 기타 Spring 내부 예외 및 최종 예외 처리 (시그니처 변경 및 로직 개선)
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {

        log.error("Internal server error occurred", ex);

        ResponseCode responseCode = ResponseCode.of(statusCode);
        String message = body instanceof String ? (String) body : ex.getMessage();

        CommonResponse<String> commonResponse = CommonResponse.<String>builder()
                .code(responseCode)
                .message(responseCode.getMessage())
                .data(Objects.toString(message, "An unexpected error occurred"))
                .build();

        return new ResponseEntity<>(commonResponse, headers, statusCode);
    }
}