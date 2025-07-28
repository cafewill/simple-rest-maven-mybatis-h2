package com.cube.simple.interceptor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SimpleInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle (HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		
        HttpServletRequest  wrappedRequest  = wrapRequest(request);
        HttpServletResponse wrappedResponse = wrapResponse(response);

        log.info ("Check : SimpleInterceptor.preHandle => Methd={}, URI={}, Handler={}, Body={}"
        		, wrappedRequest.getMethod()
        		, wrappedRequest.getRequestURI()
        		, handler
        		, extractRequestBody(wrappedRequest));

        return HandlerInterceptor.super.preHandle (request, response, handler);
	}
	
	@Override
	public void postHandle (HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        ContentCachingResponseWrapper wrappedResponse = Objects.nonNull(WebUtils.getNativeRequest(request, ContentCachingResponseWrapper.class))
                ? (ContentCachingResponseWrapper) response
                : (ContentCachingResponseWrapper) wrapResponse(response);

		log.info ("Check : SimpleInterceptor.postHandle => Status={}, URI={}, Body={}"
                , wrappedResponse.getStatus()
                , request.getRequestURI()
                , Objects.toString(extractResponseBody(wrappedResponse), "{}"));
		
		HandlerInterceptor.super.postHandle (request, response, handler, modelAndView);
	}
	
    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) throws Exception {

        ContentCachingResponseWrapper wrappedResponse = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (Objects.isNull (wrappedResponse)) {
            wrappedResponse = (ContentCachingResponseWrapper) wrapResponse(response);
        }

		log.info ("Check : SimpleInterceptor.afterCompletion => Status={}, URI={}, Body={}"
                , wrappedResponse.getStatus()
                , request.getRequestURI()
                , Objects.toString(extractResponseBody(wrappedResponse), "{}"));

        // 반드시 호출하여 원본 응답 스트림에 데이터를 복사
        wrappedResponse.copyBodyToResponse();
    }
    
    private HttpServletRequest wrapRequest(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper) {
            return request;
        }
        return new ContentCachingRequestWrapper(request);
    }

    private HttpServletResponse wrapResponse(HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper) {
            return response;
        }
        return new ContentCachingResponseWrapper(response);
    }

    private String extractRequestBody(HttpServletRequest request) throws UnsupportedEncodingException {
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (Objects.isNull(wrapper)) {
            return null;
        }
        byte[] buf = wrapper.getContentAsByteArray();
        if (buf.length == 0) {
            return null;
        }
        return new String(buf, 0, buf.length, wrapper.getCharacterEncoding());
    }

    private String extractResponseBody(ContentCachingResponseWrapper response) throws IOException {
        byte[] buf = response.getContentAsByteArray();
        if (buf.length == 0) {
            return null;
        }
        return new String(buf, 0, buf.length, response.getCharacterEncoding());
    }	
}
