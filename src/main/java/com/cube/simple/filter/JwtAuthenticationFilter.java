package com.cube.simple.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cube.simple.dto.CommonResponse;
import com.cube.simple.enums.ResponseCode;
import com.cube.simple.util.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${common.error.data:false}")
    private boolean showErrorData;

    private final JWTUtil jwtUtil;

    private final ObjectMapper objectMapper;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        log.info ("Check : header [{}] {}", header);

        if (Objects.nonNull(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = jwtUtil.parseToken(token);
                String username = claims.getSubject();
                String role = claims.get("role", String.class);
                log.info ("Check : role [{}], token [{}]", role, token);
                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(username, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                SecurityContextHolder.getContext().setAuthentication(auth);
	        } catch (ExpiredJwtException ex) {
	            // 1) detail 생성
	            String detail = ex.getLocalizedMessage();
	
	            // 2) 로그
	            log.info("JWT 토큰 만료 : {}", detail);
	
	            // 3) CommonResponse 생성
	            CommonResponse<String> body = CommonResponse.<String>builder()
	                .code(ResponseCode.UNAUTHORIZED)                     // enum 에 정의된 코드
	                .message(ResponseCode.UNAUTHORIZED.getMessage())     // “토큰이 만료되었습니다.” 와 같이 세팅
	                .data(showErrorData ? detail : null)                 // 상세 메시지 노출 여부
	                .build();
	
	            // 4) JSON 응답
	            writeJsonResponse(response, HttpStatus.UNAUTHORIZED, body);
	            return;
	
	        } catch (JwtException ex) {
	            String detail = ex.getLocalizedMessage();
	            log.info("JWT 검증 실패 : {}", detail);
	
	            CommonResponse<String> body = CommonResponse.<String>builder()
	                .code(ResponseCode.UNAUTHORIZED)
	                .message(ResponseCode.UNAUTHORIZED.getMessage())   // “유효하지 않은 토큰입니다.” 등
	                .data(showErrorData ? detail : null)
	                .build();
	
	            writeJsonResponse(response, HttpStatus.UNAUTHORIZED, body);
	            return;
	        }            
            
        }
        filterChain.doFilter(request, response);
    }
    
    private void writeJsonResponse(HttpServletResponse response,
            HttpStatus status,
            CommonResponse<?> body) throws IOException {

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(ResponseCode.UNAUTHORIZED.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }    
}