package com.cube.simple.config;

/**
 * Spring Security의 @PreAuthorize 등에서 사용할 SpEL 표현식을 상수로 관리하는 클래스입니다.
 */
public final class SecurityExpressions {

    private SecurityExpressions() {
        // 인스턴스화 방지
    }

    // 각 역할(Role)에 대한 권한 확인 표현식
    public static final String HAS_ROLE_ADMIN = "hasRole('ADMIN')";
    public static final String HAS_ROLE_OWNER = "hasRole('OWNER')";
    public static final String HAS_ROLE_USER = "hasRole('USER')";

    // 여러 역할을 확인하는 표현식
    public static final String HAS_ANY_ROLE_USER_ADMIN = "hasAnyRole('USER', 'ADMIN')";

    // 특정 사용자 본인 또는 ADMIN만 허용하는 표현식
    public static final String IS_SELF_OR_ADMIN = "#id == authentication.name or hasRole('ADMIN')";
}