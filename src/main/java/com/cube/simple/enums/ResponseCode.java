package com.cube.simple.enums;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

/**
 * 공통 응답 코드 관리용 Enum
 */
@Getter
public enum ResponseCode {

    // 2xx Success
    SUCCESS(HttpStatus.OK, "요청에 성공했습니다."),
    CREATED(HttpStatus.CREATED, "리소스가 생성되었습니다."),

    // 4xx Client Error
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청하신 리소스를 찾을 수 없습니다."),

    // 5xx Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

	// Extra
    FOUND(HttpStatus.FOUND, "Found"),		// 임시 리다이렉트
    NOT_EXISTS(HttpStatus.NOT_FOUND, "Not Exists"),			// 엔티티 미존재
    FAILURE(HttpStatus.BAD_REQUEST, "Fail"),					// 클라이언트 요청 오류
    ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");   	// 서버 에러
	
	
    private final HttpStatus httpStatus;
    private final String message;
    private final int code;

    ResponseCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
        // 비즈니스 코드로 HTTP 스테이터스 코드 활용 (원하면 별도 커스텀 코드를 부여해도 무방)
        this.code = Objects.requireNonNull(httpStatus, "HttpStatus must not be null").value();
    }

    /**
     * HttpStatus → ResponseCode 매핑 (없으면 INTERNAL_ERROR 반환)
     */
    public static ResponseCode of(HttpStatusCode statusCode) {
        for (ResponseCode rc : values()) {
            if (rc.httpStatus.equals(statusCode)) {
                return rc;
            }
        }
        return INTERNAL_SERVER_ERROR;
    }
}
