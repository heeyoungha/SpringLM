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

**구현 완료 사항:**
- **웹 + REST API 이중 구조**: 동일한 기능을 웹 페이지와 REST API 두 방식으로 제공
- **게시글 CRUD**: 작성, 조회, 수정, 삭제 
- **게시글 목록**: 페이징 처리 
- **검색 기능**: 제목 부분검색 지원 
- **정렬**: 기본 최신순 (id DESC)
- **댓글 기능**: 게시글 상세에서 댓글 목록/등록/수정/삭제
- **소프트 삭제**: 논리적 삭제로 데이터 보존
- **시드 데이터**: 애플리케이션 시작 시 게시글 10건 자동 생성
- **JPA Auditing**: 생성/수정 시간 자동 관리
- **예외 처리**: 커스텀 예외 클래스로 일관된 오류 처리

**REST API 특징:**
- **RESTful 설계**: HTTP 메서드(GET, POST, PUT, DELETE) 적절한 사용
- **JSON 통신**: 요청/응답 모두 JSON 형태로 처리
- **상태 코드**: 적절한 HTTP 상태 코드 반환 (200, 201, 400, 404 등)
- **리소스 중심**: `/api/board`, `/api/board/{id}` 등 명확한 리소스 URL
- **페이징 지원**: API에서도 Pageable 파라미터로 페이징 처리

**파일 구조:**
```
src/main/java/com/example/springlm/
├── SpringLmApplication.java # 메인 애플리케이션
├── board/
│ ├── Board.java # 게시글 엔티티
│ ├── BoardDto.java # 게시글 DTO
│ ├── BoardRepository.java # 게시글 Repository
│ ├── BoardService.java # 게시글 Service
│ ├── BoardWebController.java # 웹 컨트롤러
│ ├── BoardApiController.java # REST API 컨트롤러
│ └── reply/
│ ├── Reply.java # 댓글 엔티티
│ ├── ReplyDto.java # 댓글 DTO
│ ├── ReplyRepository.java # 댓글 Repository
│ ├── ReplyService.java # 댓글 Service
│ ├── ReplyWebController.java # 댓글 웹 컨트롤러
│ └── ReplyApiController.java # 댓글 API 컨트롤러
├── common/
│ ├── BaseEntity.java # 공통 엔티티 (Auditing)
│ ├── ServiceUtil.java # 공통 유틸리티
│ └── exception/
│ └── DomainException.java # 커스텀 예외
├── config/
│ └── BoardSeeder.java # 시드 데이터 생성
└── user/
├── User.java # 사용자 엔티티
└── UserRepository.java # 사용자 Repository
src/main/resources/
├── application.properties # Spring 설정
├── templates/
│ └── board/
│ ├── get-boardlist.html # 게시글 목록
│ ├── create-board.html # 게시글 작성
│ ├── edit.html # 게시글 수정
│ └── board-detail.html # 게시글 상세 + 댓글
└── static/
└── css/
└── common.css # 스타일시트
```
**실행 방법:**
```bash
# 애플리케이션 실행
./gradlew bootRun

# 또는 JAR 파일 실행
./gradlew build
java -jar build/libs/SpringLM-0.0.1-SNAPSHOT.jar

# 브라우저에서 접속
http://localhost:8080/boardList     # 게시판 목록
http://localhost:8080/h2-console    # H2 데이터베이스 콘솔
```

**주요 기술 스택:**
- Spring Boot 3.5.5 + Java 17
- Spring Data JPA + Hibernate
- Thymeleaf 템플릿 엔진
- H2 Database (개발용)
- Lombok (코드 간소화)
- Gradle (빌드 도구)

**API 엔드포인트:**
```
GET /boardList # 게시글 목록 (웹)
GET /api/board # 게시글 목록 (API)
POST /api/board # 게시글 작성
GET /board/{id} # 게시글 상세
PUT /board/edit/{id} # 게시글 수정
DELETE /board/{id} # 게시글 삭제
POST /api/board/{id}/reply # 댓글 작성
PUT /api/board/{id}/reply/{rid} # 댓글 수정
DELETE /board/{id}/reply/{rid} # 댓글 삭제
```
## 🚀 3단계 - RESTful API 및 로그인 구현

**목표:** 현대 웹 개발 핵심인 REST API 및 인증 기능 구현 능력

**주요 구현 사항:**
- `/api/tasks` 리소스 CRUD 구현 (`GET`, `POST`, `PUT`, `DELETE`)
- JSON 요청/응답 처리 (`@RequestBody`, `@ResponseBody`)
- 로그인 기능: POST 요청 처리, 세션/JWT 기반 인증
- 로그인 성공 시 HTTP 상태 코드 302 및 `Set-Cookie` 헤더 설정
- Spring Security 또는 커스텀 Filter 활용


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