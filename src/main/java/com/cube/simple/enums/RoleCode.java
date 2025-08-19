package com.cube.simple.enums;

public enum RoleCode {
	
	// 추가 또는 변경시 SecurityConfig, SecurityExpressions 및 @PreAuthorize 사용된 코드도 함께 리뷰필!
	
    ADMIN ("관리자"),
    OWNER ("판매자"),
    USER  ("사용자");

    private final String description;

    RoleCode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
