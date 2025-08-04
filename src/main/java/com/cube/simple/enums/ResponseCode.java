package com.cube.simple.enums;

public enum ResponseCode {

    SUCCESS(200, "Success"),               	// 정상 처리
    FAILURE(400, "Fail"),					// 클라이언트 요청 오류
    ERROR(500, "Internal Server Error"),   	// 서버 에러

	NOT_FOUND(404, "Not Found"),			// 리소스 미발견
    NOT_EXISTS(404, "Not Exists"),			// 엔티티 미존재

    FOUND(302, "Found"),					// 임시 리다이렉트
    BAD_REQUEST(400, "Bad Request"),		// 잘못된 요청
    UNAUTHORIZED(401, "Unauthorized"),		// 인증 필요
    FORBIDDEN(403, "Forbidden");			// 권한 없음

    private final int status;
    private final String message;

    ResponseCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getDefaultMessage() {
        return message;
    }
}
