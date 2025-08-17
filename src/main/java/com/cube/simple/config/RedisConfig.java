package com.cube.simple.config;

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 캐시 설정
 *
 * - @EnableCaching: @Cacheable, @CachePut, @CacheEvict 등 캐시 어노테이션 활성화
 * - @ConditionalOnProperty: application properties 에서 spring.cache.type=redis 일 때만 이 설정이 적용됨
 *   (로컬/테스트에서 메모리 캐시를 쓰고 싶으면 spring.cache.type=simple 로 두면 이 설정이 로딩되지 않음)
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = false)
public class RedisConfig {

    /**
     * Redis 캐시 공통 설정
     * - 키 직렬화: String (사람이 읽기 쉬움, CLI에서도 명확)
     * - 값 직렬화: GenericJackson2JsonRedisSerializer (클래스 정보 포함 JSON 직렬화)
     * - TTL: 기본 1분 (필요 시 조정)
     *
     * ⚠️ JdkSerializationRedisSerializer 를 쓰지 않는 이유:
     *   - 바이너리 형태라 가독성이 떨어지고, 클래스 변경에 취약
     *   - JSON 직렬화가 시스템 간 호환 및 디버깅에 유리
     */
    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            // 캐시 키는 문자열로 직렬화
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
            )
            // 캐시 값은 JSON으로 직렬화(클래스 타입 정보 포함)
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
            )
            // 기본 TTL (모든 캐시에 적용). 필요 시 per-cache 설정에서 덮어쓰기 가능
            .entryTtl(Duration.ofMinutes(1));
    }

    /**
     * RedisCacheManager
     * - Spring Cache 추상화를 통해 Redis 를 캐시 저장소로 사용
     * - 위에서 정의한 기본 설정(cacheConfiguration)을 모든 캐시에 적용
     *
     * 필요 시 withInitialCacheConfigurations(Map<String, RedisCacheConfiguration>) 을 통해
     * 캐시 이름별 TTL/직렬화 전략을 개별 지정할 수 있음.
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        return RedisCacheManager.builder(factory)
            .cacheDefaults(cacheConfiguration())
            .build();
    }
}
