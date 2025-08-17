# 캡틴 백엔드 템플릿 프로젝트

(진행중이며 임의 변경 및 삭제 될 수 있음)

> 현재 JWT 기반 인증 및 Redis, Kafka, RabbitMQ 연동, DB 읽기/쓰기 분리 구조를 적용 중입니다.

## 개발 환경

| 항목      | 내용                                    |
| ------- | ------------------------------------- |
| 언어      | Java 17, Spring Boot 3.4.8            |
| 데이터베이스  | MySQL (MyBatis)                       |
| 아키텍처    | MSA (우선 단일 서비스로 개발 후 주요 실행 단위로 분리 예정) |
| 네이밍 컨벤션 | CamelCase 및 풀네임 사용                    |
| 개발 가이드  | Swagger 활용                            |
| 주요 리소스  | AWS RDS, S3, Redis, Kafka, RabbitMQ 등 |


## 코드 구현 가이드

테이블 등 데이터 구성에 따른 Model 구현
Model 에 따른 Mapper, Service 및 Controller 구현
(필요시) Request 및 Response DTO 구현

* **Model 구현 예시**:
`com/cube/simple/model/Demo.java`

* **Request 및 Response DTO 구현 예시**:
`com/cube/simple/dto/DemoRequest.java`,
`com/cube/simple/dto/DemoResponse.java`

* **Mapper 구현 예시 (Read/Write DB 분리 구성)**:
`resources/mapper/*/*DemoMapper.xml`,
`com/cube/simple/mapper/*/*DemoMapper.java`

* **Service 구현 예시**:
`com/cube/simple/service/DemoService.java`

* **Controller 구현 예시**:
`com/cube/simple/controller/DemoController.java`
(Swagger 관련 어노테이션 정보 구현필, 복붙 및 ChatGPT 활용하여 업데이트하면됨)

* **Common Request / Response 구현 예시**:
`com/cube/simple/dto/CommonRequest.java`
`com/cube/simple/dto/CommonResponse.java`
(요청 및 응답에 대한 일관성 확보를 위하여 공통 객체를 활용함)
: 기본은 공통 객체로 처리하된 필요시 LoginRequest, DemoRequest, DemoResponse 등과 같이 커스텀 구현함)
: 오류 응답시 CommonResponse.data 에 exception 정보 등 상세 정보가 설정되며 개발 환경에서는 common.error.data=true 설정해야 확인 가능함 (운영 환경에서 시스템 환경, 알고리즘 구성 등 민감 정보 노출 방지를 위함)
: 예시 : "data": "Cannot invoke \"com.cube.simple.model.Member.getPassword()\" because \"found\" is null",

* **Validation 구현 예시**:
`com/cube/simple/dto/CommonRequest.java, DemoRequest.java`
`com/cube/simple/model/Item.java, Demo.java`
`com/cube/simple/controller/ItemController.java, DemoController.java`
(Generic 으로 설정된 데이터 검증을 위한 @Valid 활용건)

```java
// Model 객체에 Bean Validation Constraint (빈 검증 제약) 설정함
public class Item {
    
    @NotNull (message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    @Max(value = 1000000, message = "가격은 1,000,000 이하이어야 합니다.")
    private Long price;
    
    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "카테고리는 필수입니다.")
    private String category;

    // ... ...
}

// @Valid 어노테이션 설정한 Generic 타입의 공통 요청 활용 (필요시 DemoRequest 와 같은 추가 커스텀 요청 활용함)
public class CommonRequest <T> {
    
    @Valid
    @NotNull(message = "Request <T> 는 필수입니다.")
    private T data;
    
    @Builder.Default  
    private final LocalDateTime timestamp = LocalDateTime.now(Clock.systemUTC());
}

// Controller 의 Create 메소드에 @Valid 어노테이션 설정 및 Generic 타입으로 넘어오는 객체를 명시해야 Validation 정상 동작함
public ResponseEntity<?> insert(@Valid @RequestBody CommonRequest <Item> request) {
    // ... ...
}
```

## 프로파일(Profile) 설정 안내

Spring Boot 애플리케이션 실행 시, 개발 도구(STS, IntelliJ 등)에서 활성화할 프로파일을 지정해야 합니다.

* 기본 프로파일: `local`
* 사용 가능한 프로파일: `local`, `develop`, `production`

```bash
# 로컬 실행 예시
$ mvn spring-boot:run -Dspring-boot.run.profiles=local

# IntelliJ 실행 예시 (VM Options)
-Dspring.profiles.active=develop
```

### application-\*.properties 구성

각 프로파일별로 별도의 `application-{profile}.properties` 파일을 사용하여 설정을 분리합니다.
아래는 `develop` 프로파일의 예시입니다. `local` 및 `production`도 동일한 키를 사용하되, 값만 달리 설정합니다.

### 1. JWT & AES

```properties
jwt.secret=[${profile} JWT secret key]
jwt.expiration=1200000
aes.key=[${profile} AES key]
```

* `jwt.secret`: JWT 서명용 비밀 키
* `jwt.expiration`: 토큰 만료 시간(ms)
* `aes.key`: AES 암호화 키

: 필요시 AES Key, JWT Secret 생성 활용 : AESUtil.generateKey, JWTUtil.generateSecret

### 2. AWS S3

```properties
cloud.aws.credentials.access-key=[${profile} AWS access key]
cloud.aws.credentials.secret-key=[${profile} AWS secret key]
cloud.aws.region.static=[${profile} AWS region]
cloud.aws.s3.bucket=[${profile} S3 bucket name]
```

* AWS S3 파일 업로드/다운로드용 설정

### 3. Cache (Redis)

```properties
spring.cache.type=redis
spring.data.redis.host=[${profile} Redis host]
spring.data.redis.port=[${profile} Redis port]
spring.data.redis.password=[${profile} Redis password]
```

* Redis를 캐시 저장소로 사용

### 4. DB 설정

* 테스트용 초기화 DB (생략 가능)

```properties
spring.datasource.url=[${profile} write DB URL]
# spring.datasource.driver-class-name=[${profile} JDBC driver]
spring.datasource.username=[${profile} DB 사용자]
spring.datasource.password=[${profile} DB 비밀번호]
```

* Read 전용 DB

```properties
spring.datasource.read.jdbc-url=[${profile} read DB URL]
# spring.datasource.read.driver-class-name=[${profile} JDBC driver]
spring.datasource.read.username=[${profile} read DB 사용자]
spring.datasource.read.password=[${profile} read DB 비밀번호]
```

* Write 전용 DB

```properties
spring.datasource.write.jdbc-url=[${profile} write DB URL]
# spring.datasource.write.driver-class-name=[${profile} JDBC driver]
spring.datasource.write.username=[${profile} write DB 사용자]
spring.datasource.write.password=[${profile} write DB 비밀번호]
```

## Read/Write DB 분리 구성

본 프로젝트는 읽기(Read)와 쓰기(Write) 작업에 대해 분리된 DataSource 및 SqlSession을 구성합니다.

### application.properties 예시:

```properties
# Read DB
spring.datasource.read.jdbc-url=jdbc:h2:mem:testdb
spring.datasource.read.username=sa
spring.datasource.read.password=

# Write DB
spring.datasource.write.jdbc-url=jdbc:h2:mem:testdb
spring.datasource.write.username=sa
spring.datasource.write.password=
```

### Java 설정 클래스 예시:

* `ReadDataSourceConfig.java` / `WriteDataSourceConfig.java`

```java
@Configuration
@MapperScan(basePackages = "com.cube.simple.mapper.read", sqlSessionTemplateRef = "readSqlSessionTemplate")
public class ReadDataSourceConfig {
    @Bean(name = "readDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.read")
    public DataSource readDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "readSqlSessionFactory")
    public SqlSessionFactory readSqlSessionFactory(@Qualifier("readDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setTypeAliasesPackage("com.cube.simple.model");
        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/read/*.xml"));
        return sessionFactory.getObject();
    }

    @Bean(name = "readSqlSessionTemplate")
    public SqlSessionTemplate readSqlSessionTemplate(@Qualifier("readSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
```

### Read/Write Mapper 인터페이스 분리:

```java
public interface ReadDemoMapper {
    Demo selectById(Long id);
    List<Demo> selectAll();
}

public interface WriteDemoMapper {
    int insertDemo(Demo demo);
    int updateDemo(Demo demo);
    int deleteById(Long id);
}
```

### (개발자 테스트 및 데모용) DB 초기화 자동 처리:

```java
@Component
public class DataInitializer {
    private final DataSource writeDataSource;

    @PostConstruct
    public void initialize() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("schema.sql"));
        populator.addScript(new ClassPathResource("data.sql"));
        populator.execute(writeDataSource);
    }
}

@Component
public class MemberInitializer {

    @Autowired
    private WriteMemberMapper writeMemberMapper;

    @PostConstruct
    public void initialize() {
        writeMemberMapper.insert(Member.builder()
            .role("ADMIN")
            .id("admin")
            .password(encrypt("2580"))
            .name("관리자")
            .description("모든 권한 관리자")
            .build());
        writeMemberMapper.insert(Member.builder()
                .role("HOST")
                .id("host")
                .password(encrypt("8282"))
                .name("판매자")
                .description("모든 권한 관리자")
                .build());
        writeMemberMapper.insert(Member.builder()
            .role("USER")
            .id("jeju")
            .password(encrypt("1234"))
            .name("제주")
            .description("일반 권한 사용자")
            .build());
    }
}
```

### ✅ 사용자 계정 정보

| 계정 구분 | ID    | PW   | 설명        |
| ----- | ----- | ---- | --------- |
| 관리자   | admin | 8282 | 모든 권한 관리자 |
| 판매자   | host  | 8282 | 셀러 권한 사용자 |
| 사용자   | jeju  | 1234 | 일반 권한 사용자 |

### 🔐 API 접근 권한

| API 경로       | 설명                  | 인증 필요 | 권한     |
| ------------ | ------------------- | ----- | ------ |
| `/api/**`    | 전체 생성, 수정, 삭제 (CUD) | ✅     | 관리자 허용 |
| `/api/items` | Item 조회 (R)         | ✅     | 사용자 이상 |
| `/api/demos` | Demo 조회 (R)         | ❌     | 모두 허용  |

: 기본은 SecurityConfig.java 에서 관리함
: 필요시 MemberController.java 에서와 같이 관리자만 @PreAuthorize("hasRole('ADMIN')") 또는 해당 사용자 추가 @PreAuthorize("hasRole('ADMIN') or #id == authentication.name") 설정함

### 🔒 JWT 관련 정보

* 유틸리티: `/src/main/java/com/cube/simple/util/JwtUtil.java`
* 필터: `/src/main/java/com/cube/simple/filter/JwtAuthenticationFilter.java`
* 보안 설정: `/src/main/java/com/cube/simple/config/SecurityConfig.java`

### 개인 정보 등 데이터 암복호화 구현 안내

이 프로젝트는 AES 알고리즘을 이용해 민감한 데이터를 자동으로 암복호화합니다.  
아래 컴포넌트와 어노테이션을 활용해 간편하게 적용할 수 있습니다:

1. **Aspect 클래스**  
- `/src/main/java/com/cube/simple/aspect/AES*.java`
- `/src/main/java/com/cube/simple/aspect/SHA*.java`  
: 메서드 실행 전후에 `@SHAEncrypt`, `@AESEncrypt`, `@AESDecrypt` 어노테이션을 감지하여 데이터 변환을 수행합니다.

2. **모델 어노테이션**  
- `/src/main/java/com/cube/simple/model/Member.java`  
   
```java
@Data
public class Member {
 
	@SHAData
	@NotBlank(message = "비밀번호는 필수입니다.")
	private String password;

	@AESData
	private String name;
	@AESData
	private String phone;
     // (... ...)
}
```
: `@SHAData`를 붙인 클래스의 모든 String 필드에 대해 암호화를 처리합니다. (비밀번호 단방향 암호화)
: `@AESData`를 붙인 클래스의 모든 String 필드에 대해 암복호화를 처리합니다. (민감 정보 데이터에 대한 암복호화)

3. **서비스 레벨 어노테이션**  
- `/src/main/java/com/cube/simple/service/MemberService.java`  
```java
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberMapper memberMapper;

    @AESEncrypt // @AESData 로 설정된 항목 암호화
	@SHAEncrypt // @SAHData 로 설정된 항목 암호화
	@Transactional
	public void insert (Member member)

     @AESDecrypt  // @AESData 로 설정된 항목 복호화
     public Member selectById(Long id)
     
     @AESDecrypt  // @AESData 로 설정된 항목 복호화
     public List <Member> selectAll ()
}
```

4. **초기 데이터 삽입 예제**  
- `/src/main/java/com/cube/simple/init/MemberInitializer.java`  
: 애플리케이션 시작 시 샘플 `Member` 데이터를 암호화하여 DB에 저장합니다.
   
### Cache 구현 안내 (Redis 또는 로컬 메모리 Cache)

CRUD API 성능 향상을 위해 Spring Cache 추상화를 사용하여 Redis 또는 로컬 메모리 캐시를 지원합니다.

1. **서비스 / 컨트롤러 예시**  
- `/src/main/java/com/cube/simple/service/CachedItemService.java`  
- `/src/main/java/com/cube/simple/controller/CachedItemController.java`  

```java
@Service
@RequiredArgsConstructor
public class CachedItemService {
   
	@CacheEvict(cacheNames = {"items", "itemCount"}, allEntries = true)
	public void insert (Item item)
   
	@Cacheable(cacheNames = "items", key = "#page + '-' + #size + '-' + (#category?:'') + '-' + (#search?:'')")
	public List <Item> selectAll (int page, int size, String category, String search)

	@Cacheable(cacheNames = "item", key = "#id")
	public Item selectById (Long id)
		
	@Caching(evict = {
	            @CacheEvict(cacheNames = "item",      key = "#item.id"),
	            @CacheEvict(cacheNames = "items",     allEntries = true),
	            @CacheEvict(cacheNames = "itemCount", allEntries = true)
	        })
	public void update (Item item)
		
	@Caching(evict = {
	            @CacheEvict(cacheNames = "item",      key = "#id"),
	            @CacheEvict(cacheNames = "items",     allEntries = true),
	            @CacheEvict(cacheNames = "itemCount", allEntries = true)
	        })
	public void deleteById (Long id)
}
```

## 다국어(i18n) 설정 및 활용 가이드

다국어 지원을 위해 Spring의 MessageSource와 LocaleResolver를 활용하는 방법을 안내합니다.

### 1. 메시지 프로퍼티 파일 생성

프로젝트 리소스 디렉터리(`src/main/resources`)에 다음 파일들을 생성합니다.

```
messages.properties          # 기본 메시지
messages_ko.properties       # 한국어 메시지
messages_en.properties       # 영어 메시지
```

예시 내용:

```properties
# messages_ko.properties
welcome=반갑습니다 : {0}
api.response.ok=성공 응답
api.response.fail=실패 응답
api.welcome.summary=환영 메시지 및 클라이언트 IP 반환
api.welcome.description=요청 헤더를 통해 클라이언트 IP를 추출하고, WelcomeResponse 로 반환함

# messages_en.properties
welcome=Welcome : {0}
api.response.ok=Successful response
api.response.fail=Failed response
api.welcome.summary=Retrieve multilingual welcome message
api.welcome.description=Returns a greeting based on the Accept-Language header
```

### 2. 설정 클래스 추가

`com.cube.simple.config.I18nConfig.java`를 생성하고 아래와 같이 설정합니다.

```java
@Configuration
public class I18nConfig implements WebMvcConfigurer {

    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
        ms.setBasename("i18n/messages");
        ms.setDefaultEncoding("UTF-8");
        ms.setFallbackToSystemLocale(false);
        ms.setCacheSeconds(10);
        return ms;
    }
     
    // (... ...)
}
```

### 3. 컨트롤러에서 메시지 사용 예시

`com.cube.simple.controller.WelcomeController.java`를 생성하고 아래와 같이 구현합니다.

```java
package com.cube.simple.controller;

@RestController
@RequiredArgsConstructor
@Slf4j
public class WelcomeController {

    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    @GetMapping("/welcome")
    @Operation(
            summary     = "{api.welcome.summary}",
            description = "{api.welcome.description}"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "{api.response.ok}", content = @Content(schema = @Schema(implementation = WelcomeResponse.class)))
    })
    public ResponseEntity<?> welcome(HttpServletRequest request, Locale locale) {
        
        String clientIp = extractClientIp(request);
        String message = messageSource.getMessage("welcome", new Object[]{clientIp}, locale);

        WelcomeResponse<?> response = WelcomeResponse.builder()
                                                .code(ResponseCode.SUCCESS)
                                                .message(message)
                                                .build();
        log.info("Check welcome => locale : {}, message : {}", locale, response.getMessage());
        return ResponseEntity.ok(response);
    }
}
```

### 4. 요청 예시
- 영어: `GET /welcome?lang=en` → “{"code":"SUCCESS","message":"Welcome : 0:0:0:0:0:0:0:1","timestamp":"2025-08-04T08:06:57.6074935"}”
- 한국어: `GET /welcome?lang=ko` → “{"code":"SUCCESS","message":"반갑습니다 : 0:0:0:0:0:0:0:1","timestamp":"2025-08-04T08:06:23.2986979"}”

## REST API 규격서 가이드

Controller 구현시 Swagger 관련 어노테이션 정보 구현하면 자동 반영됨
URL : `[API SERVER]/swagger-ui/index.html`

## 공통 추가 개발 이슈

* (완료함) `/mapper/read/*`, `/mapper/write/*` 등 RW 데이터베이스 분리 구성안 반영
* (완료함) 다국어 코드 구현 반영 (한국어, 중국어, 필요시 영어 등)
* (완료함) 사용자 ROLE 권한 반영 (ADMIN 관리자, HOST 판매자, USER 사용자 등 필요시 DB 적재 및 로드함)
* (완료함) 프런트 - 백엔드 Request / Response 연동 방식 반영 (기본 응답 구조 및 다국어 이슈 등)
* (완료함) 각종 Config, Filter, Interceptor 및 AOP 코드 구현
* (완료함) 암호, 이름, 주소, 주민번호, 전화번호 등 민감정보에 대한 SHA 암호화 및 AES 암복호화를 코드 구현
* (완료함) S3, Redis, Firebase 연동 코드 코드 구현함
* (진행중) Kafka, RabbitMQ 등 주요 리소스 활용을 위한 코드 구현 (대부분 공통 코드일거고 필요시 개별 비즈니스 로직을 위한 인터페이스 구현 예정)
* (진행중, 단 개발에 집중하고 이후 운영 환경 마련후 진행해도 무방함) 실행 환경 프로필 및 로깅 설정 반영 (develop, production 필요시 staging 등)
* (필요시, 자체 서비스용이고, 대외 공개 API 는 아니기에 불필요 하겠음) `/api/v1/*`, `/api/v2/*` 등 버전 관리 기능 반영

## REST API 오퍼레이션 네이밍 가이드

URI에 CRUD 를 의미하는 동사를 사용하지 않고, HTTP Request Method 를 사용함
(\* 필요시 operation-path 사용함)

```
POST   [API SERVER]/api/operation-path/items         (아이템 생성)
GET    [API SERVER]/api/operation-path/items         (전체 아이템 목록 조회)
GET    [API SERVER]/api/operation-path/items/{id}    (단일 아이템 정보 조회)
PUT    [API SERVER]/api/operation-path/items/{id}    (아이템 정보 수정)
DELETE [API SERVER]/api/operation-path/items/{id}    (아이템 삭제)
```

추천 예시 (HTTP Request Method 에 CRUD 를 내포한다고 봄, 규격서 문서화에도 깔끔함)

```
GET, POST, PUT or PATCH, DELETE 
[API SERVER]/api/operation-path/items
```

비추 예시 (규격서 문서화시 설명이 길어짐)

```
POST   [API SERVER]/api/operation-path/createItem
GET    [API SERVER]/api/operation-path/selectItem
GET    [API SERVER]/api/operation-path/selectItems
PUT    [API SERVER]/api/operation-path/updateItem
DELETE [API SERVER]/api/operation-path/deleteItem
```

Java code 예시 (단 Java 코드에서 메소드 구현시엔 CRUD 를 의미하는 네이밍 명명 필요함, 파라미터 + 메소드 명이 동일하면 오류남)

```java
@PostMapping
createItem 
    String createSql;
    Map <String, Object> createResult = new HashMap <> ();
    try { ... } catch (SomeException createException) { ... }

@GetMapping or @GetMapping("/{id}")
selectAll or selectById (Long {id})  
    String selectSql;
    Map <String, Object> selectResult = new HashMap <> ();
    try { ... } catch (SomeException selectException) { ... }

@PutMapping("/{id}")
updateItem (String {id})
    String updateSql;
    Map <String, Object> updateResult = new HashMap <> ();
    try { ... } catch (SomeException updateException) { ... }

@DeleteMapping("/{id}")
deleteItem (Long {id})
    String deleteSql;
    Map <String, Object> deleteResult = new HashMap <> ();
    try { ... } catch (SomeException deleteException) { ... }
```

## RESTful API 설계 시 자주 권장되는 명명(Naming) 규칙과 모범 사례 참고

### 1. 리소스(Resource) 명명

* **명사(Noun) 사용**

  * URI에는 행위(동사)가 아니라 리소스를 나타내는 명사만 사용
  * 예) `/users` (○), `/getUsers` (×)
* **복수형(Plural) 사용 권장**

  * 컬렉션은 복수형: `/users`, `/orders`
  * 단일 리소스는 ID 사용: `/users/{userId}`
* **소문자 & 케밥 케이스(kebab-case)**

  * 예) `/user-profiles`, `/order-items`

### 2. HTTP 메서드(Method) 매핑

| 행위 | HTTP 메서드  | URI 예시                  | 설명          |
| -- | --------- | ----------------------- | ----------- |
| 생성 | POST      | `/users`                | 새로운 사용자 생성  |
| 조회 | GET       | `/users`, `/users/{id}` | 전체/단일 조회    |
| 수정 | PUT/PATCH | `/users/{id}`           | 전체 또는 일부 수정 |
| 삭제 | DELETE    | `/users/{id}`           | 리소스 삭제      |

* PUT: 전체 교체 (replace)
* PATCH: 일부 갱신 (update)

### 3. 계층 관계 표현

* 부모-자식: `/users/{userId}/orders`
* 3단 이상 중첩은 지양

### 4. 쿼리 파라미터(Query Parameters)

* 필터링: `/orders?status=paid&createdAfter=2025-07-01`
* 페이지네이션: `/products?page=2&size=20`
* 정렬: `/products?sort=price,asc`
* 검색: `/articles?search=REST+API`

### 5. 버전 관리(Versioning)

* URI: `/v1/users`, `/v2/users`
* 또는 HTTP 헤더: `Accept: application/vnd.myapi.v1+json`

### 6. 페이로드(Payload) 명명

```json
{
  "userId": 123,
  "firstName": "Jane",
  "lastName": "Doe"
}
```

* 날짜/시간: ISO 8601 형식

```json
"2025-07-22T16:30:00+09:00"
```

### 7. 상태 코드(Status Codes)

* 200 OK, 201 Created, 204 No Content
* 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found
* 500 Internal Server Error

### 8. 에러 응답 형식

```json
{
  "timestamp": "2025-07-22T16:32:10+09:00",
  "status": 404,
  "error": "Not Found",
  "message": "User not found",
  "path": "/v1/users/999"
}
```

### 9. HATEOAS (선택 사항)

```json
{
  "userId": 123,
  "firstName": "Jane",
  "links": [
    { "rel": "self", "href": "/v1/users/123" },
    { "rel": "orders", "href": "/v1/users/123/orders" }
  ]
}
```

### 참고자료

* [Resource Naming (RESTful API) – RESTfulAPI.net](https://restfulapi.net/resource-naming/)
* [Microsoft REST API Guidelines](https://learn.microsoft.com/azure/architecture/best-practices/api-design)
* [Google Cloud API Design Guide](https://cloud.google.com/apis/design/)
* [JSON API Specification](http://jsonapi.org/)
* [Swagger (OpenAPI) Best Practices](https://swagger.io/resources/articles/best-practices-in-api-design/)
* [Richardson Maturity Model – Martin Fowler](https://martinfowler.com/articles/richardsonMaturityModel.html)
* [Roy Fielding’s REST Dissertation](https://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm)
* [Postman Blog: RESTful API Best Practices](https://blog.postman.com/best-practices-for-building-a-scalable-restful-api/)
