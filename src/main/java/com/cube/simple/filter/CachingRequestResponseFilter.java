package com.cube.simple.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CachingRequestResponseFilter extends OncePerRequestFilter {

 private static final String CHARACTER_ENCODING = StandardCharsets.UTF_8.name();

 @Override
 protected void doFilterInternal(HttpServletRequest request,
                                 HttpServletResponse response,
                                 FilterChain filterChain) throws ServletException, IOException {
     // 요청과 응답을 캐싱하기 위해 Wrapper 사용
     ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
     ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

     long startTime = System.currentTimeMillis();
     filterChain.doFilter(wrappedRequest, wrappedResponse);
     long duration = System.currentTimeMillis() - startTime;

     // Request 로그 출력
     log.info("[REQUEST] method={} uri={} clientIp={} body={}",
             wrappedRequest.getMethod(),
             wrappedRequest.getRequestURI(),
             wrappedRequest.getRemoteAddr(),
             extractRequestBody(wrappedRequest)
     );

     // Response 로그 출력
     log.info("[RESPONSE] method={} uri={} durationMs={} status={} body={}",
             wrappedRequest.getMethod(),
             wrappedRequest.getRequestURI(),
             duration,
             wrappedResponse.getStatus(),
             extractResponseBody(wrappedResponse)
     );

     // 캐싱된 응답 내용을 실제 응답 스트림으로 다시 복사
     wrappedResponse.copyBodyToResponse();
 }

 private String extractRequestBody(ContentCachingRequestWrapper request) {
     byte[] buf = request.getContentAsByteArray();
     if (buf.length == 0) {
         return "";
     }
     return new String(buf, StandardCharsets.UTF_8);
 }

 private String extractResponseBody(ContentCachingResponseWrapper response) {
     byte[] buf = response.getContentAsByteArray();
     if (buf.length == 0) {
         return "";
     }
     return new String(buf, StandardCharsets.UTF_8);
 }
}

