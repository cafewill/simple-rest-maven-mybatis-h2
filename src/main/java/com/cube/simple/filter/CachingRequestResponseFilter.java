package com.cube.simple.filter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
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
@Order(Ordered.LOWEST_PRECEDENCE)
public class CachingRequestResponseFilter extends OncePerRequestFilter {

    private static final Set<String> EXCLUDE_PREFIXES = Set.of(
        "/swagger-ui", "/v3/api-docs", "/webjars", "/favicon"
    );
    private static final int MAX_BODY = 4 * 1024; // 4KB

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return EXCLUDE_PREFIXES.stream().anyMatch(uri::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper resp = new ContentCachingResponseWrapper(response);

        long start = System.currentTimeMillis();
        Exception error = null;

        try {
            chain.doFilter(req, resp);
        } catch (Exception ex) {
            error = ex;
            throw ex;
        } finally {
            long duration = System.currentTimeMillis() - start;

            // --- 요청 로그 ---
            String clientIp = extractClientIp(req);
            String reqCt = defaultString(req.getContentType(), "");
            String reqBody = safeRequestBody(req, reqCt, req.getCharacterEncoding(), req.getRequestURI());

            log.info("[REQUEST] method={} uri={} query={} clientIp={} contentType={} headers={} body={}",
                req.getMethod(),
                req.getRequestURI(),
                defaultString(req.getQueryString(), ""),
                clientIp,
                reqCt,
                maskedHeaders(req),
                reqBody
            );

            // --- 응답 로그 ---
            String respCt = defaultString(resp.getContentType(), "");
            String respBody = safeResponseBody(resp, respCt);

            log.info("[RESPONSE] method={} uri={} durationMs={} status={} contentType={} body={}",
                req.getMethod(),
                req.getRequestURI(),
                duration,
                resp.getStatus(),
                respCt,
                respBody
            );

            // 응답 바디가 클라이언트로 실제로 나가도록 반드시 호출
            resp.copyBodyToResponse();
        }
    }

    private String safeRequestBody(ContentCachingRequestWrapper req, String contentType, String charset, String uri) {
        if (isSensitiveUri(uri)) return "(masked)";
        if (!isTextual(contentType)) return "(skipped: " + contentType + ")";
        byte[] buf = req.getContentAsByteArray();
        if (buf.length == 0) return "";
        return truncate(decode(buf, charset));
    }

    private String safeResponseBody(ContentCachingResponseWrapper resp, String contentType) {
        if (!isTextual(contentType)) return "(skipped: " + contentType + ")";
        byte[] buf = resp.getContentAsByteArray();
        if (buf.length == 0) return "";
        return truncate(decode(buf, resp.getCharacterEncoding()));
    }

    private boolean isTextual(String contentType) {
        if (contentType == null) return false;
        try {
            MediaType mt = MediaType.parseMediaType(contentType);
            if ("text".equals(mt.getType())) return true;
            if (MediaType.APPLICATION_JSON.includes(mt)) return true;
            if (MediaType.APPLICATION_XML.includes(mt)) return true;
            if (MediaType.APPLICATION_PROBLEM_JSON.includes(mt)) return true;
            if (MediaType.APPLICATION_FORM_URLENCODED.includes(mt)) return true;
            return false; // image/*, font/*, octet-stream 등은 false
        } catch (InvalidMediaTypeException e) {
            return false;
        }
    }

    private String decode(byte[] bytes, String charset) {
        try {
            if (charset != null && !charset.isBlank()) {
                return new String(bytes, Charset.forName(charset));
            }
        } catch (Exception ignore) {}
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private String truncate(String s) {
        if (s.length() <= MAX_BODY) return s;
        return s.substring(0, MAX_BODY) + "...(truncated)";
    }

    private boolean isSensitiveUri(String uri) {
        return uri.startsWith("/api/auth"); // 필요 시 추가: /login, /password 등
    }

    private String maskedHeaders(HttpServletRequest req) {
        List<String> names = Collections.list(req.getHeaderNames());
        Map<String, String> map = new LinkedHashMap<>();
        for (String n : names) {
            String v = req.getHeader(n);
            if (n.equalsIgnoreCase("authorization") || n.equalsIgnoreCase("cookie") || n.equalsIgnoreCase("set-cookie")) {
                map.put(n, "****");
            } else {
                map.put(n, v);
            }
        }
        return map.toString();
    }

    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) return realIp.trim();
        return request.getRemoteAddr();
    }

    private String defaultString(String s, String dft) {
        return (s == null) ? dft : s;
    }
}

