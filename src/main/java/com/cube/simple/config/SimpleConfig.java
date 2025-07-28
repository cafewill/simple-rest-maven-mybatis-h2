package com.cube.simple.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.cube.simple.interceptor.SimpleInterceptor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class SimpleConfig implements WebMvcConfigurer {

	@Autowired
	SimpleInterceptor simpleInterceptor;
	
    @Override
    public void addInterceptors (InterceptorRegistry registry) {
    	
        log.info ("Check : SimpleConfig.addInterceptors ()");
    	
        registry.addInterceptor (simpleInterceptor)
	        .addPathPatterns("/api/**")
	        .excludePathPatterns(
	            "/api/swagger-resources/**",
	            "/api/swagger-ui.html",
	            "/api/v2/api-docs",
	            "/api/webjars/**"
	        );
    }
}
