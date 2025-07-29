package com.cube.simple.config;

import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class I18nConfig {

	 @Bean
	 public ResourceBundleMessageSource messageSource() {
	     ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
	     // src/main/resources/i18n/messages*.properties
	     ms.setBasename("i18n/messages");
	     ms.setDefaultEncoding("UTF-8");
	     ms.setFallbackToSystemLocale(false);
		 ms.setCacheSeconds(10);
	     return ms;
	 }

	 @Bean
	 public LocaleResolver localeResolver() {
	     SessionLocaleResolver slr = new SessionLocaleResolver();
	     slr.setDefaultLocale(Locale.KOREAN);
	     return slr;
	 }

	 @Bean
	 public LocaleChangeInterceptor localeChangeInterceptor() {
	     LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
	     lci.setParamName("lang");
	     return lci;
	 }

	 @Bean
	 public WebMvcConfigurer localeInterceptorConfig(LocaleChangeInterceptor lci) {
	     return new WebMvcConfigurer() {
	         @Override
	         public void addInterceptors(InterceptorRegistry reg) {
	             reg.addInterceptor(lci);
	         }
	     };
	 }
}
