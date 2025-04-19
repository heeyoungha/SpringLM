## Spring 기반 게시판 시스템 (2단계)

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


