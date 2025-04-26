## 테스트 코드 작성 및 기능 검증 보완

**목표:** 단순 기능 구현을 넘어서 **테스트 코드로 안정성 확보**

### 🧪 테스트 환경 구축
- **테스트 프로파일**: `application-test.yml`
  - 자동 적용: ./gradlew test 실행 시 application-test.yml이 자동 로드되어 테스트 프로파일 활성화
  - DB: H2 인메모리 데이터베이스 사용, 매 테스트마다 create-drop으로 스키마 초기화
  - JWT: 테스트 전용 시크릿 키 및 1시간 만료 시간 설정
  - → 별도의 환경 구성 없이 테스트 실행만으로 일관된 환경에서 안정적으로 검증 가능
- **테스트 도구**
  - **JUnit 5 (Jupiter)**: 테스트 프레임워크
  - **Spring Boot Test**: `@SpringBootTest`, `@WebMvcTest`
  - **Mockito**: Repository, Service 모킹
  - **H2 Database**: 인메모리 DB 기반 데이터 검증
  - **Testcontainers** *(선택)*: 실제 DB(PostgreSQL/MySQL) 기반 통합 테스트
  - **Jacoco**: 테스트 커버리지 리포트
  - 
#### 1) Unit Test (핵심 로직 검증) ✅ **구현 완료**

- **JwtUtilTest**: JWT 토큰 생성/검증/만료 여부/Claims 파싱 검증 (11개 테스트)
- **UserServiceTest**: 사용자 CRUD 및 예외 처리 검증 (10개 테스트)
- **GoogleResponseTest**: OAuth2 응답 파싱 및 null-safe 처리 검증 (8개 테스트)
- **성과**: 총 29개 단위 테스트 작성 → 코드 품질 개선 (버그 수정 & null-safe 강화)

##### **🔧 Unit Test를 통한 실제 코드 품질 개선**

1. **UserService.createUser() 버그 수정**:
   ```java
   // 수정 전: save 반환값을 사용하지 않음 (ID가 null)
   userRepository.save(user);
   return new UserResponse(user);
   
   // 수정 후: save 반환값 사용 (ID 포함된 저장된 엔티티)
   User savedUser = userRepository.save(user);
   return new UserResponse(savedUser);
   ```

2. **JWT 로직 중복 제거 및 단일 책임 원칙 적용**:
   - **기존**: `CustomOAuth2UserService`, `JwtAuthenticationFilter`에 JWT 로직 중복
   - **개선**: `JwtUtil` 클래스로 통합하여 코드 중복 제거
   ```java
   // CustomOAuth2UserService에서 JwtUtil 사용
   return jwtUtil.generateToken(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
   
   // JwtAuthenticationFilter에서 JwtUtil 사용  
   if (token != null && jwtUtil.validateToken(token)) {
       Claims claims = jwtUtil.getClaimsFromToken(token);
   ```

3. **GoogleReponse null 안전성 강화**:
   ```java
   // 수정 전: NullPointerException 위험
   return attribute.get("email").toString();
   
   // 수정 후: null-safe 처리
   Object email = attribute.get("email");
   return email != null ? email.toString() : null;
   ```

---

#### 2) Repository Layer 테스트 (Data Layer 안정성 검증)
- **UserRepository 테스트**: `UserRepositoryTest.java`
  - `@DataJpaTest` 어노테이션 활용
  - 사용자 저장, 조회, 중복 이메일 검증
  - 커스텀 쿼리 메소드(`findByEmail()`) 검증

---

#### 3) Web Layer 테스트 (Controller & Security)
- **LoginController 테스트**: `LoginControllerTest.java`
  - `@WebMvcTest` 활용
  - 로그인 페이지 렌더링 테스트
  - 인증된/비인증된 사용자 접근 제어 검증
- **Security 설정 테스트**: `SecurityConfigTest.java`
  - `@SpringBootTest` + `TestRestTemplate` 활용
  - OAuth2 로그인 엔드포인트 접근 검증
  - JWT 토큰 필터 동작 확인
- **JwtAuthenticationFilter 테스트**: `JwtAuthenticationFilterTest.java`
  - 유효한 JWT → 인증 성공
  - 잘못된 JWT → 인증 실패

---

#### 4) Integration Test & End-to-End 테스트 (전체 플로우 검증)
- **OAuth2 통합 테스트**: `OAuth2IntegrationTest.java`
  - `@SpringBootTest(webEnvironment = RANDOM_PORT)`
  - `MockWebServer`로 Google OAuth2 API 모킹
  - 전체 인증 플로우 시나리오 검증
- **API 통합 테스트**: `AuthenticationIntegrationTest.java`
  - TestContainers로 실제 DB(PostgreSQL/MySQL) 연결
  - 로그인 → JWT 발급 → 인증된 API 호출 플로우 검증
- **환경변수 테스트**: `EnvironmentConfigTest.java`
  - `.env` 파일 로딩 및 Spring 프로퍼티 바인딩 검증
  

### 🛠 테스트에서 활용한 Spring 기술 스택

#### **🧪 테스트 프레임워크 & 라이브러리**
- **JUnit 5 (Jupiter)**: 단위/통합 테스트 프레임워크
  - `@Test`, `@DisplayName`, `@BeforeEach` - 테스트 메서드 및 설정
  - `@ExtendWith(MockitoExtension.class)` - Mockito 통합 (순수 Unit Test)
- **Mockito**: Mock 객체 생성 및 검증
  - `@Mock` - Repository 등 의존성 Mock 생성 (`UserRepository`)
  - `@InjectMocks` - 테스트 대상 객체에 Mock 주입 (`UserService`, `JwtUtil`)
  - `given().willReturn()` - Mock 동작 정의 (BDD 스타일)
  - `then().should()` - Mock 호출 검증
  - `willAnswer()` - 동적 Mock 응답 설정 (ID 생성 시뮬레이션)
  - `never()` - 메서드 호출되지 않음 검증
- **AssertJ**: 유창한 검증 API 
  - `assertThat()` - 값 검증, `isEqualTo()`, `isNotNull()`, `hasSize()`
  - `assertThatThrownBy()` - 예외 발생 검증
  - `assertThatCode()` - 예외 미발생 검증

#### **🍃 Spring Test 관련 기술**
- **Spring Test Utils**: 
  - `ReflectionTestUtils.setField()` - `@Value` private 필드 주입 (Unit Test에서 사용)
- **Spring Boot Test**:
  - `@SpringBootTest` - 통합 테스트용 (전체 ApplicationContext 로드)
  - `@ExtendWith(MockitoExtension.class)` - 순수 Unit Test (Context 로드 없이 빠른 실행)
  - `@Disabled` - 특정 테스트 비활성화 (통합 테스트 제외)
- **Test Profile 설정**:
  - `src/test/resources/application-test.yml` - 테스트 전용 설정 파일
  - `spring.profiles.active: test` - 테스트 프로파일 활성화
  - `systemProperty("spring.profiles.active", "test")` - Gradle 테스트 태스크 설정

#### **💾 테스트 데이터베이스**
- **H2 Database**: 
  - `jdbc:h2:mem:testdb` - 인메모리 테스트 DB
  - `ddl-auto: create-drop` - 테스트 시작/종료 시 스키마 생성/삭제
  - `DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE` - 테스트 간 DB 연결 유지
  - `show-sql: false` - 테스트 로그 최소화

#### **🔐 테스트 보안 설정**
- **OAuth2 Test Configuration**: 
  - `test_google_client_id`, `test_google_client_secret` - 테스트용 더미 값
- **JWT Test Configuration**: 
  - `test_jwt_secret_key_for_testing_32_chars_minimum` - HS512 알고리즘용 64바이트 이상 키
  - `expiration: 3600000` - 테스트용 1시간 만료 시간
- **Logging Configuration**: 
  - `root: WARN`, `com.example.springlm: INFO` - 테스트 로그 레벨 최적화

---

## 📊 테스트 커버리지 리포트 (Jacoco)

### **테스트 실행 및 커버리지 리포트 생성**

```bash
# 모든 테스트 실행
./gradlew test

# Jacoco 커버리지 리포트 생성
./gradlew jacocoTestReport

# 테스트 실행 + 커버리지 리포트 생성 (한 번에)
./gradlew clean test jacocoTestReport
```

### **📁 리포트 파일 위치**

```
build/
├── jacocoHtml/           # HTML 형식 커버리지 리포트
│   ├── index.html       # 메인 리포트 페이지
│   └── com.example.springlm.*/  # 패키지별 상세 리포트
├── jacoco/              # 바이너리 커버리지 데이터
│   └── test.exec        # 테스트 실행 데이터
└── reports/tests/test/  # 테스트 실행 결과
    └── index.html       # 테스트 결과 리포트
```

### **🌐 리포트 확인 방법**

1. **브라우저에서 HTML 리포트 열기:**
   ```
   file:///[프로젝트경로]/build/jacocoHtml/index.html
   ```

2. **macOS에서 바로 열기:**
   ```bash
   open build/jacocoHtml/index.html
   ```

3. **리포트에서 확인할 수 있는 정보:**
   - **패키지별 커버리지**: 각 패키지의 라인/브랜치 커버리지 비율
   - **클래스별 상세 분석**: 메서드별 커버리지 현황
   - **소스코드 하이라이팅**: 테스트된/안된 코드 구분 표시
   - **커버리지 통계**: 전체 프로젝트 커버리지 요약

### **📈 커버리지 목표 설정**

`build.gradle`에서 최소 커버리지 비율 설정:

```gradle
jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = 0.80  // 80% 이상 커버리지 목표
            }
        }
    }
}
```

### **✅ 현재 구현된 테스트들**

- **🔐 JwtUtilTest**: JWT 토큰 생성/검증/파싱 로직 (11개 테스트)
- **👤 UserServiceTest**: 사용자 CRUD 비즈니스 로직 (10개 테스트)  
- **🌐 GoogleResponseTest**: OAuth2 응답 데이터 변환 (8개 테스트)

**총 29개 테스트** 모두 통과 ✅

### **🚀 테스트 실행 명령어**

```bash
# 특정 테스트 클래스만 실행
./gradlew test --tests "JwtUtilTest"
./gradlew test --tests "UserServiceTest" 
./gradlew test --tests "GoogleResponseTest"

# 모든 Unit Test 실행
./gradlew test --tests "*Test"

# 테스트 프로파일로 애플리케이션 실행
./gradlew bootRunTest

# 커버리지 검증 (최소 80% 체크)
./gradlew jacocoTestCoverageVerification
```
