# HTTP 서버 + 웹 서비스 프로젝트 (Spring + QueryDSL)

## 🚀 1단계 - 간단한 HTTP 서버 구현 (순수 Java) ✅ 

**목표:** 소켓을 활용한 서버의 기본 구조와 동작원리 이해

**구현 완료 사항:**
- `ServerSocket`을 사용한 서버 시작 및 클라이언트 연결 처리
- HTTP 요청 파싱(GET 지원)
- 간단한 정적 파일 서비스 (HTML, CSS, JS)
- HTTP 상태 코드 반환 (200 OK, 404 Not Found)
- 기본적인 라우팅 처리 (루트 경로 → index.html)
- QueryString 파싱 및 파라미터 추출
- 웹 인터페이스를 통한 서버 응답 테스트

**파일 구조:**
```
src/main/java/com/example/springlm/step1/simplehttpserver/
├── SimpleHttpServer.java          # 메인 서버 클래스
└── server-step1.md               # 구현 가이드 문서

public/
├── simpleserver.html             # 웹 테스트 인터페이스
├── style.css                     # 스타일시트
```

**실행 방법:**
```bash
# 서버 실행
java -cp src/main/java com.example.springlm.step1.simplehttpserver.SimpleHttpServer

# 브라우저에서 접속
http://localhost:8080/simpleserver.html
```

## 🚀 2단계 -Spring 기반 게시판 시스템 구현 (웹 + REST API) ✅
**목표:** Spring을 활용한 게시판 CRUD 및 검색/페이징 기능 구현

### 주요 구현 사항
- [x] 게시글 CRUD: 작성, 조회, 수정, 삭제 (웹/REST API)
- [x] 게시글 목록: 페이징 처리
- [x] 검색 기능: 제목 부분검색 지원
- [x] 정렬: 기본 최신순 (id DESC)
- [x] 댓글 기능: 게시글 상세에서 댓글 목록/등록/수정/삭제
- [x] 시드 데이터: 앱 시작 시 게시글 10건 자동 생성 (`BoardSeeder`)

### 기술 포인트

#### 🏗️ **프레임워크 & 아키텍처**
- **Spring Boot 3.5.5** + **Java 17**: 최신 버전 기반 개발
- **@SpringBootApplication**: 자동 설정 및 컴포넌트 스캔
- **계층형 아키텍처**: Controller → Service → Repository 분리
- **@Controller vs @RestController**: 웹 뷰와 REST API 분리 설계

#### 🗄️ **데이터베이스 & 영속성**
- **Spring Data JPA**: 데이터 접근 계층 추상화
- **@EnableJpaAuditing**: 엔티티 생성/수정 시간 자동 관리
- **BaseEntity**: `@MappedSuperclass`로 공통 필드(createdAt, updatedAt) 상속
- **@PrePersist/@PreUpdate**: JPA 생명주기 콜백 활용
- **H2 Database**: 개발용 인메모리 DB + MySQL 연결 지원
- **Query Method**: `findByTitleContaining()` 등 메서드명 기반 쿼리 생성
- **@PageableDefault**: 페이징 기본값 설정 (size, sort, direction)

#### 🔒 **엔티티 설계 & 고급 기능**
- **Soft Delete**: `@SQLDelete`, `@Where` 어노테이션으로 논리적 삭제
- **연관관계 매핑**: `@OneToMany`, `@ManyToOne` + `@JoinColumn`
- **FetchType.LAZY**: 지연 로딩으로 성능 최적화
- **CascadeType**: 영속성 전이 설정 (PERSIST, MERGE)
- **orphanRemoval**: 고아 객체 자동 제거

#### 🎨 **프론트엔드 & 템플릿**
- **Thymeleaf**: 서버사이드 템플릿 엔진
    - `th:object`, `th:field`: 폼 바인딩
    - `th:each`: 반복 처리
    - `th:if`, `th:unless`: 조건부 렌더링
    - `th:replace`: Fragment 재사용 (`~{fragment/nav :: navigation}`)
- **Ajax + jQuery**: 비동기 댓글 CRUD 처리
- **Fetch API**: 게시글 작성/수정/삭제 처리
- **CSS Grid/Flexbox**: 반응형 레이아웃

#### 🔧 **개발 도구 & 유틸리티**
- **Lombok**: `@Getter`, `@Builder`, `@RequiredArgsConstructor` 등 보일러플레이트 코드 제거
- **SLF4J**: `LoggerFactory.getLogger()` 로깅 시스템
- **ServiceUtil**: 공통 유틸리티 클래스
    - `findByIdOrThrow()`: Optional 처리 간소화
    - `formatDateTime()`, `truncateText()`: 데이터 포맷팅
- **DomainException**: 커스텀 예외 클래스 + 정적 팩토리 메서드

#### ⚡ **성능 & 최적화**
- **@Transactional**: 트랜잭션 경계 설정
    - `readOnly = true`: 읽기 전용 최적화
- **페이징 처리**: `Page<T>`, `Pageable` 인터페이스 활용
- **DTO 패턴**: 엔티티와 뷰 계층 분리
- **Builder 패턴**: 불변 객체 생성

#### 🚀 **시드 데이터 자동 추가**
- **ApplicationRunner**: `BoardSeeder`로 초기 데이터 자동 생성
    - `run(ApplicationArguments args)`: 애플리케이션 시작 완료 후 실행
    - 조건부 데이터 생성: `boardRepository.count() == 0` 체크

#### 📦 **빌드 & 의존성**
- **Gradle**: 빌드 도구 + 의존성 관리
- **Spring Boot Starter**: Web, JPA, Thymeleaf, Validation 스타터 활용

### 엔드포인트 요약
- 웹(Thymeleaf)
    - GET `/boardList` (페이징, 제목검색)
    - GET `/board` (작성 폼)
    - POST `/board` (작성)
    - GET `/board/{id}` (상세 + 댓글 목록)
    - GET `/board/edit/{id}` (수정 폼)
    - PUT `/board/edit/{id}` (수정)
    - DELETE `/board/{id}` (삭제)
    - GET `/board/{boardId}/reply/new` (댓글 작성 폼)
    - POST `/board/{boardId}/reply` (댓글 등록)
    - GET `/board/{boardId}/reply/{replyId}/edit` (댓글 수정 폼)
    - POST `/board/{boardId}/reply/{replyId}/edit` (댓글 수정)
    - DELETE `/board/{boardId}/reply/{replyId}` (댓글 삭제)
- REST API
    - GET `/api/board` (페이징, 제목검색: `searchTitle`)
    - POST `/api/board` (작성)
    - GET `/api/board/{boardId}` (조회)
    - PUT `/api/board/{boardId}` (수정)
    - POST `/api/board/{boardId}/reply` (댓글 등록)
    - PUT `/api/board/{boardId}/reply/{replyId}` (댓글 수정)

### 파일 구조 (요약)
```
src/main/java/com/example/springlm/
├── board/
│   ├── Board.java
│   ├── BoardDto.java
│   ├── BoardRepository.java
│   ├── BoardService.java
│   ├── BoardWebController.java
│   ├── BoardApiController.java
│   └── reply/
│       ├── Reply.java
│       ├── ReplyDto.java
│       ├── ReplyRepository.java
│       ├── ReplyService.java
│       └── ReplyApiController.java
├── common/
│   ├── BaseEntity.java
│   └── ...
└── config/
    └── BoardSeeder.java

src/main/resources/
├── templates/
│   └── board/
│       ├── get-boardlist.html      # 게시글 목록 (페이징, 제목검색)
│       ├── create-board.html       # 게시글 작성
│       ├── edit.html               # 게시글 수정
│       └── board-detail.html       # 게시글 상세 + 댓글
├── static/
│   └── css/
│       └── common.css
└── application.properties          # Spring 설정
```

## 🚀 3단계 - Spring OAuth2 + JWT 인증 시스템

**목표:** OAuth2와 JWT를 활용한 Google 소셜 로그인 기능 구현

### 주요 구현 사항
- [x] OAuth2 Google 로그인: Google 계정 연동
- [x] JWT 인증: 토큰 기반 사용자 인증 및 권한 관리
- [x] Spring Security: 보안 설정 및 인증 필터 구현
- [x] 사용자 관리: User 엔티티 및 관련 서비스

### 기술 포인트

#### 🔐 **인증 & 보안**
- **Spring Security**: 웹 보안 프레임워크
  - `SecurityConfig`: 보안 설정 클래스
  - `@EnableWebSecurity`: 웹 보안 활성화
- **OAuth2 소셜 로그인**:
  - Google OAuth2 연동 (`GoogleResponse`)
  - `CustomOAuth2UserService`: 커스텀 OAuth2 사용자 서비스
- **JWT (JSON Web Token)**: 토큰 기반 인증
  - `JwtAuthenticationFilter`: JWT 인증 필터
  - 토큰 생성, 검증, 파싱 기능
- **세션 관리**: 로그인 상태 유지 및 관리

#### 🔧 **환경변수 & 보안 설정**
- **Dotenv 라이브러리**: `io.github.cdimascio:java-dotenv:5.2.2`
  - `.env` 파일에서 민감한 정보 관리 (OAuth2 클라이언트 ID/Secret, JWT Secret Key)
  - 소스코드에서 보안 정보 분리
- **통합 환경변수 관리 패턴**:
  - `.env` 파일에서 **통일된 변수명** 사용
  - `SpringApplication` 실행 **전** 모든 환경변수를 시스템 프로퍼티로 로드
  - Spring의 `${GOOGLE_CLIENT_ID}`, `${JWT_SECRET_KEY}` placeholder와 연동
  - **이 방식의 장점**
    - **개발/운영 환경별 다른 `.env` 파일** 사용 가능
    - **Spring Boot 표준 준수**: `${변수명}` placeholder 패턴 사용
    - **보안성**: 민감한 정보를 코드베이스에서 완전 분리
    - **컨테이너 친화적**: Docker/K8s 환경에서 환경변수로 오버라이드 가능

#### 👤 **사용자 관리**
- **User 엔티티**: 사용자 정보 모델링
- **UserService**: 사용자 관련 비즈니스 로직
- **UserRepository**: 사용자 데이터 접근 계층
- **DTO 패턴**:
  - `UserRequest`: 사용자 요청 데이터
  - `UserResponse`: 사용자 응답 데이터
  - `OAuth2Response`: OAuth2 응답 인터페이스
  - `GoogleResponse`: Google OAuth2 응답 처리

### 엔드포인트 요약
#### 인증 관련
- GET `/` (메인 페이지)
- GET `/login` (로그인 페이지)
- GET `/oauth2/authorization/google` (Google OAuth2 로그인)
- POST `/logout` (로그아웃)

### 파일 구조
```
src/main/java/com/example/springlm/
├── user/
│   ├── User.java                    # 사용자 엔티티
│   ├── UserRepository.java          # 사용자 데이터 접근 계층
│   ├── UserService.java             # 사용자 비즈니스 로직
│   ├── CustomOAuth2UserService.java # OAuth2 사용자 서비스
│   └── dto/
│       ├── UserRequest.java         # 사용자 요청 DTO
│       ├── UserResponse.java        # 사용자 응답 DTO
│       ├── OAuth2Response.java      # OAuth2 응답 인터페이스
│       └── GoogleResponse.java      # Google OAuth2 응답 처리
├── login/
│   └── LoginController.java         # 로그인 컨트롤러
├── config/
│   ├── SecurityConfig.java          # Spring Security 설정
│   └── JwtAuthenticationFilter.java # JWT 인증 필터
├── common/
│   ├── BaseEntity.java              # 공통 엔티티
│   └── exception/
│       └── DomainException.java     # 커스텀 예외
└── SpringLmApplication.java         # 메인 애플리케이션

src/main/resources/
├── templates/
│   ├── index.html                   # 메인 페이지
│   └── login/
│       └── login.html               # 로그인 페이지
├── static/
│   └── css/
│       └── common.css               # 공통 스타일
├── application.yml                  # Spring 설정 (OAuth2 포함)
└── .env                            # 환경 변수 (Google OAuth2 클라이언트 정보)
```

### OAuth2 Google 로그인 구현 특징
1. **Google OAuth2 연동**: Google 계정을 통한 소셜 로그인
2. **JWT 토큰 기반 인증**: 세션 대신 JWT 토큰으로 사용자 인증
3. **Spring Security 통합**: OAuth2와 JWT를 Spring Security에 통합
4. **사용자 정보 자동 저장**: Google에서 받은 사용자 정보를 DB에 자동 저장
5. **환경 변수 보안**: OAuth2 클라이언트 정보를 `.env` 파일로 안전하게 관리


### 4단계 - 데이터베이스 연동 (JPA + QueryDSL)
**목표:** DB 처리 및 동적 쿼리 작성 능력

**주요 구현 사항**
- Spring Data JPA를 사용한 엔티티 CRUD
- QueryDSL을 활용하여 동적 검색, 조건부 조회, 복잡한 JOIN 쿼리 구현
- 트랜잭션 관리 (`@Transactional`)
- 환경별 DB 설정 (`application-dev.properties`, `application-prod.properties`)

---

### 5단계 - 코드 구조 개선 (리팩터링)

**목표:** 유지보수 가능한 설계 능력

**주요 구현 사항**
- Controller / Service / Repository 계층 분리
- 인터페이스 기반 설계
- RequestMapping + HandlerMethodResolver로 if-else 제거
- 글로벌 예외 처리 (@ControllerAdvice)

## 🚀 6단계 - 보안 처리

**목표:** 실무 필수 보안 기능 구현 능력

**주요 구현 사항:**
- JWT 토큰 생성/검증 및 만료 처리
- 비밀번호 해싱 (BCryptPasswordEncoder)
- 권한 기반 접근 제어 (USER, ADMIN 역할)
- Spring Security Filter 적용
- CORS, CSP 등 보안 헤더 설정

---

## 🚀 7단계 - 외부 API 연동

**목표:** 마이크로서비스 환경에서의 API 통신 능력

**주요 구현 사항:**
- Spring RestTemplate 또는 WebClient 사용
- 공공데이터 API 연동 (날씨, 지역 정보 등)
- API 키 환경변수/설정 파일 관리
- 장애 대응 및 Fallback 처리
- 캐싱: Spring Cache 또는 커스텀 LRU Cache

---

## 🚀 8단계 - 성능 최적화 및 동시성

**목표:** 운영 환경 수준의 성능 처리 능력

**주요 구현 사항:**
- ExecutorService 기반 멀티스레드 처리
- DB 커넥션 풀 활용(HikariCP)
- 동시성 제어: ConcurrentHashMap, AtomicInteger 활용
- 응답 시간, 메모리 사용량, 활성 스레드 수 모니터링
- 부하 테스트: JMeter, Apache Bench
- 메모리 캐시 구현 및 성능 최적화