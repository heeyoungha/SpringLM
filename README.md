# 프로젝트 개발 단계별 정리

## ✅ 1단계 - HTTP 서버 (순수 Java)
- ServerSocket 기반 HTTP 서버 직접 구현  
- 정적 파일 서비스 (HTML, CSS, JS) + QueryString 파싱  
- 요청 라우팅 및 HTTP 상태코드(200, 404) 처리  

[👉 1단계 구현사항 확인](https://github.com/heeyoungha/SpringLM/tree/step1)

---

## ✅ 2단계 - Spring 게시판 시스템
- 게시판 CRUD + 댓글 기능 (웹 + REST API)  
- 페이징, 검색, 정렬 구현 (최신순)  
- 엔티티 고급 기능: Soft Delete, 지연 로딩, Cascade, orphanRemoval  
- Thymeleaf + Ajax로 프론트엔드 처리  
- BoardSeeder로 초기 데이터 자동 생성  

[👉 2단계 구현사항 확인](https://github.com/heeyoungha/SpringLM/tree/step2)

---

## ✅ 3단계 - OAuth2 + JWT 인증
- Google OAuth2 소셜 로그인 + JWT 발급/검증  
- Spring Security + JwtAuthenticationFilter 기반 인증/인가  
- 사용자 정보 DB 자동 저장  
- .env 파일 기반 환경변수 보안 관리  

[👉 3단계 구현사항 확인](https://github.com/heeyoungha/SpringLM/tree/step3)

---

## ✅ 4단계 - 테스트 코드 & 기능 검증
- 70개 이상 테스트 코드 작성 (Unit + Repository + Web + Integration)  
- Testcontainers 기반 MySQL 환경 → 운영 환경과 유사한 검증  
- JWT, Security, OAuth2, Board CRUD 전체 시나리오 테스트 완료  

[👉 4단계 구현사항 확인](https://github.com/heeyoungha/SpringLM/tree/step4)

---

## 📝 TO DO LIST 

### 🔐 보안 처리

- [ ] JWT 토큰 생성/검증 및 만료 처리  
- [ ] 비밀번호 해싱 (BCryptPasswordEncoder)  
- [ ] 권한 기반 접근 제어 (USER/ADMIN)  
- [ ] CORS, CSP 등 보안 헤더 설정  

### 🌐 외부 API 연동
- [ ] 공공데이터 API 연동 (예: 날씨, 지역 정보)  
- [ ] RestTemplate / WebClient 사용  
- [ ] 장애 대응 및 Fallback 처리  
- [ ] 캐싱 처리 (Spring Cache or LRU Cache)  

### ⚡ 성능 최적화 & 동시성
- [ ] ExecutorService 기반 멀티스레드 처리  
- [ ] DB 커넥션 풀 (HikariCP) 튜닝  
- [ ] 동시성 제어 (ConcurrentHashMap, AtomicInteger)  
- [ ] 부하 테스트 (JMeter, Apache Bench)  
- [ ] 메모리 캐시 적용 및 성능 모니터링  
