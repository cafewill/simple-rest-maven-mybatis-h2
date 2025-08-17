package com.cube.simple.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Read 전용 DataSource + MyBatis 설정
 *
 * - application.properties(yml)의 `spring.datasource.read.*` 값을 읽어
 *   읽기 전용 DataSource/SqlSessionFactory/SqlSessionTemplate 을 구성한다.
 * - Mapper 인터페이스는 `com.cube.simple.mapper.read` 패키지를 스캔하며,
 *   XML 매퍼는 `classpath:mapper/read/*.xml` 에 둔다.
 *
 * ⚠️ 주의
 * - 다중 DataSource 환경이라면 write 쪽 설정에 @Primary 를 두는 것이 일반적.
 * - Hikari 옵션을 쓰면 `spring.datasource.read.hikari.*` 로 세부 옵션 지정 가능.
 */
@Configuration
@MapperScan(
    basePackages = "com.cube.simple.mapper.read",      // 읽기 전용 Mapper 인터페이스 패키지
    sqlSessionTemplateRef = "readSqlSessionTemplate"   // 해당 Mapper 가 사용할 SqlSessionTemplate Bean 이름
)
public class ReadConfig {
	
    /**
     * 읽기 전용 DataSource Bean
     * - prefix 아래의 구성 값을 바인딩한다.
     *   예) spring.datasource.read.jdbc-url, username, password, driver-class-name ...
     */
    @Bean(name = "readDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.read")
    public DataSource readDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * 읽기 전용 SqlSessionFactory
     * - 위에서 만든 readDataSource 를 사용
     * - model 패키지를 type alias 로 등록 (XML에서 짧은 이름 사용 가능)
     * - XML 매퍼 위치를 지정
     */
    @Bean(name = "readSqlSessionFactory")
    public SqlSessionFactory readSqlSessionFactory(
            @Qualifier("readDataSource") DataSource dataSource) throws Exception {

        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);

        // Camel case 매핑 추가함
        org.apache.ibatis.session.Configuration cfg =
                new org.apache.ibatis.session.Configuration();
        cfg.setMapUnderscoreToCamelCase(true);
        cfg.setJdbcTypeForNull(org.apache.ibatis.type.JdbcType.NULL);
        sessionFactory.setConfiguration(cfg);
        
        // 도메인 모델의 패키지(별칭 등록)
        sessionFactory.setTypeAliasesPackage("com.cube.simple.model");

        // XML 매퍼 파일 위치 (classpath:mapper/read/*.xml)
        sessionFactory.setMapperLocations(
            new PathMatchingResourcePatternResolver()
                .getResources("classpath:mapper/read/*.xml")
        );

        return sessionFactory.getObject();
    }

    /**
     * 읽기 전용 SqlSessionTemplate
     * - MyBatis의 스레드-세이프한 SqlSession 대체 객체
     * - 위 readSqlSessionFactory 를 기반으로 생성
     */
    @Bean(name = "readSqlSessionTemplate")
    public SqlSessionTemplate readSqlSessionTemplate(
            @Qualifier("readSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {

        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
