## Spring OAuth2 + JWT 인증 시스템

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

