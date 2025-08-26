package com.cube.simple.config;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Write 전용 DataSource + MyBatis 설정
 *
 * - application.properties(yml)의 `spring.datasource.write.*` 설정으로 쓰기용 DataSource 생성
 * - Mapper 인터페이스는 `com.cube.simple.mapper.write` 패키지 스캔
 * - XML 매퍼는 `classpath:mapper/write/*.xml`
 *
 * ⚠️ 다중 DataSource 환경 팁
 * - 보통 하나의 DataSource(@Primary)를 기본으로 두고, 나머지는 이름으로 주입(@Qualifier)합니다.
 * - 트랜잭션이 필요하면 쓰기용 PlatformTransactionManager를 별도로 등록해도 됩니다.
 */
@Configuration
@MapperScan(
    basePackages = "com.cube.simple.mapper.write",      // 쓰기용 Mapper 인터페이스 위치
    sqlSessionTemplateRef = "writeSqlSessionTemplate"   // 이 Mapper들이 사용할 SqlSessionTemplate
)
public class WriteConfig {
	
    /**
     * 쓰기용 DataSource
     * - 예: spring.datasource.write.jdbc-url, username, password, driver-class-name
     * - Hikari를 사용한다면 spring.datasource.write.hikari.* 로 세부 옵션 지정 가능
     */
    @Primary
    @Bean(name = "writeDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.write")
    public DataSource writeDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * 쓰기용 SqlSessionFactory
     * - 위 writeDataSource를 연결
     * - 모델 패키지를 type alias로 등록 (XML에서 짧은 이름 사용)
     * - XML 매퍼 파일 위치 지정
     */
    @Primary
    @Bean(name = "writeSqlSessionFactory")
    public SqlSessionFactory writeSqlSessionFactory(
            @Qualifier("writeDataSource") DataSource dataSource) throws Exception {

        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);

        // Camel case 매핑 추가함
        org.apache.ibatis.session.Configuration cfg =
                new org.apache.ibatis.session.Configuration();
        cfg.setMapUnderscoreToCamelCase(true);
        cfg.setJdbcTypeForNull(org.apache.ibatis.type.JdbcType.NULL);
        sessionFactory.setConfiguration(cfg);

        // 도메인 모델 별칭 패키지 (예: <resultMap type="Member"> 등에서 클래스명만 사용 가능)
        sessionFactory.setTypeAliasesPackage("com.cube.simple.model");
        sessionFactory.setTypeHandlersPackage("com.cube.simple.handler.mybatis");
        // sessionFactory.setDatabaseIdProvider(vendorDatabaseIdProvider());        

        // 쓰기 전용 매퍼 XML 경로
        sessionFactory.setMapperLocations(
            new PathMatchingResourcePatternResolver()
                .getResources("classpath:mapper/write/**/*.xml")
        );

        return sessionFactory.getObject();
    }

    /**
     * 쓰기용 SqlSessionTemplate
     * - 스레드-세이프한 SqlSession 대체 객체
     * - 트랜잭션/예외 변환 등 스프링과의 연동 이점을 제공
     */
    @Primary
    @Bean(name = "writeSqlSessionTemplate")
    public SqlSessionTemplate writeSqlSessionTemplate(
            @Qualifier("writeSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {

        return new SqlSessionTemplate(sqlSessionFactory);
    }

    /*
    @Bean(name =  "writeDatabaseIdProvider")
    public VendorDatabaseIdProvider vendorDatabaseIdProvider() {
        var p = new java.util.Properties();
        p.setProperty("H2", "h2");        // H2
        p.setProperty("MySQL", "mysql");  // MySQL
        var v = new org.apache.ibatis.mapping.VendorDatabaseIdProvider();
        v.setProperties(p);
        return v;
    }
    */
}
