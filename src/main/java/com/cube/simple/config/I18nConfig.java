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

/**
 * 국제화(i18n) 설정.
 * - 메시지 번들 경로 및 인코딩 지정
 * - 기본 Locale 지정 (세션 기반)
 * - 요청 파라미터(lang)로 Locale 변경 허용
 * - 위 인터셉터를 MVC 체인에 등록
 */
@Slf4j
@Configuration
public class I18nConfig {

	/**
	 * 메시지 소스 설정.
	 * - classpath: i18n/messages_*.properties 를 읽는다.
	 *   예) i18n/messages_ko.properties, i18n/messages_en.properties
	 * - UTF-8 인코딩 사용
	 * - 시스템 로케일로의 자동 폴백 비활성화(명시적 번들이 없으면 코드 그대로 노출)
	 * - cacheSeconds: 개발 중 메시지 파일 변경을 빠르게 반영(운영에선 0 또는 적절히 조정)
	 */
	@Bean
	public ResourceBundleMessageSource messageSource() {
		ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
		ms.setBasename("i18n/messages");   // 메시지 번들 기본 이름(prefix)
		ms.setDefaultEncoding("UTF-8");    // 프로퍼티 파일 인코딩
		ms.setFallbackToSystemLocale(false); // OS/서버 기본 로케일로 자동 폴백하지 않음
		ms.setCacheSeconds(10);            // 10초 캐시(개발 편의). 운영은 필요 시 0/무제한 등 조정
		return ms;
	}

	/**
	 * Locale 보관 전략.
	 * - 세션에 사용자의 Locale을 저장/유지한다.
	 * - 기본 Locale은 한국어.
	 *   (쿠키 기반을 원하면 CookieLocaleResolver 사용,
	 *    완전 무상태 API면 AcceptHeaderLocaleResolver 고려)
	 */
	@Bean
	public LocaleResolver localeResolver() {
		SessionLocaleResolver slr = new SessionLocaleResolver();
		slr.setDefaultLocale(Locale.KOREAN); // 기본 로케일: ko
		return slr;
	}

	/**
	 * Locale 변경 인터셉터.
	 * - 요청 파라미터 `lang` 값으로 Locale을 변경한다.
	 *   예) /welcome?lang=en, /welcome?lang=ko
	 */
	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
		LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
		lci.setParamName("lang"); // 파라미터 이름
		return lci;
	}

	/**
	 * 위 LocaleChangeInterceptor 를 MVC 인터셉터 체인에 등록.
	 */
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
