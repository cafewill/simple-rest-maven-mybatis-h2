# 백엔드 템플릿 프로젝트

(진행중이며 임의 변경 및 삭제 될 수 있음)

> 현재 JWT 기반 인증/인가 및 Redis, Kafka, RabbitMQ 연동, DB 읽기/쓰기 분리 구조를 적용 중입니다.

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
public interface DemoReadMapper {
    Demo selectById(Long id);
    List<Demo> selectAll();
}

public interface DemoWriteMapper {
    int insertDemo(Demo demo);
    int updateDemo(Demo demo);
    int deleteById(Long id);
}
```

### (개발자 테스트 및 데모용) DB 초기화 수동 처리:

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
    public void initUsers() {
        writeMemberMapper.insert(Member.builder()
            .role("ADMIN")
            .id("admin")
            .password(encrypt("8282"))
            .name("관리자")
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

    private String encrypt(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("암호화 실패", e);
        }
    }
}
```

### ✅ 사용자 계정 정보

| 계정 구분 | ID    | PW   | 설명        |
| ----- | ----- | ---- | --------- |
| 관리자   | admin | 8282 | 모든 권한 관리자 |
| 사용자   | jeju  | 1234 | 일반 권한 사용자 |

### 🔐 API 접근 권한

| API 경로         | 설명          | 인증 필요 | 권한     |
| -------------- | ----------- | ----- | ------ |
| `/api/demos`   | Demo 조회 (R) | ❌     | 전체 허용  |
| `/api/items`   | Item 조회 (R) | ✅     | 사용자 이상 |
| `/api/*` (CUD) | 생성, 수정, 삭제  | ✅     | 차단됨    |

### 🔒 JWT 관련 정보

* 유틸리티: `JwtUtil.java`
* 필터: `JwtAuthenticationFilter.java`
* 보안 설정: `SecurityConfig.java`

## REST API 규격서 가이드

Controller 구현시 Swagger 관련 어노테이션 정보 구현하면 자동 반영됨
URL : `[API SERVER]/swagger-ui/index.html`

## 추가 개발 이슈

* (필요시) `/api/v1/*`, `/api/v2/*` 등 버전 관리 기능 반영
* 각종 Config, Filter, Interception 및 AOP 코드 구현 반영
* `/mapper/read/*`, `/mapper/write/*` 등 RW 데이터베이스 분리 구성안 반영
* Redis, Kafka, RabbitMQ 등 주요 리소스 활용을 위한 코드 구현 (대부분 공통 코드일거고 필요시 개별 비즈니스 로직을 위한 인터페이스 구현 예정)

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
