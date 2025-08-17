package com.cube.simple.enums;

public enum FirebaseCode {
	
    SUCCESS(200, "Success"),               	// 정상 처리
    PARTIAL(206, "Partial Success"),		// 다수건 처리 시 부분 성공
    FAILURE(400, "Fail"),                  	// 클라이언트 요청 오류
    ERROR(500, "Internal Server Error");   	// 서버 에러

    private final int status;
    private final String message;

    FirebaseCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}