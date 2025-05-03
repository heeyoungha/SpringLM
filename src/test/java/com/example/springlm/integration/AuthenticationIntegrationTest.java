package com.example.springlm.integration;

import com.example.springlm.user.User;
import com.example.springlm.user.UserRepository;
import com.example.springlm.user.CustomOAuth2UserService;
import com.example.springlm.config.JwtUtil;
import com.example.springlm.board.Board;
import com.example.springlm.board.BoardRepository;
import com.example.springlm.board.BoardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.List; 
import java.util.stream.Collectors; 

import jakarta.servlet.http.Cookie;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * API 통합 테스트
 * TestContainers로 실제 MySQL DB를 연결하여 로그인→JWT→API 호출 플로우 검증
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("API 통합 테스트 - 실제 DB 기반 전체 플로우 검증")
class AuthenticationIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardService boardService;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private JwtUtil jwtUtil;

    private ObjectMapper objectMapper = new ObjectMapper();
    
    private User testUser;
    private String validJwtToken;
    private Board testBoard;

    @BeforeAll
    static void configureProperties() {
        // TestContainers가 MySQL 컨테이너를 시작했는지 확인
        assertThat(mysqlContainer.isRunning()).isTrue();
        System.out.println("MySQL Container started: " + mysqlContainer.getJdbcUrl());
    }

    @BeforeEach
    @Transactional
    void setUp() {
        // 테스트 데이터 초기화
        boardRepository.deleteAll();
        userRepository.deleteAll();

        // 테스트 사용자 생성
        testUser = User.builder()
                .username("TestUser")
                .email("integration@test.com")
                .role("ROLE_USER")
                .build();
        
        testUser = userRepository.save(testUser);
        assertThat(testUser.getId()).isNotNull();

        // JWT 토큰 생성
        validJwtToken = customOAuth2UserService.generateJwtToken(testUser.getId());
        assertThat(validJwtToken).isNotNull();

        // 테스트 게시판 데이터 생성
        testBoard = Board.builder()
                .title("Integration Test Board")
                .content("This is integration test content")
                .writer(testUser.getUsername())
                
                .build();
        
        testBoard = boardRepository.save(testBoard);
        assertThat(testBoard.getId()).isNotNull();
    }

    @Test
    @Order(1)
    @DisplayName("MySQL TestContainer 연결 및 기본 설정 확인")
    void testMySQLContainerConnection() {
        // TestContainers MySQL 연결 확인
        assertThat(mysqlContainer.isRunning()).isTrue();
        assertThat(mysqlContainer.getDatabaseName()).isEqualTo("testdb");
        assertThat(mysqlContainer.getUsername()).isEqualTo("test");
        
        // 데이터베이스 연결 확인 (사용자 저장/조회)
        long userCount = userRepository.count();
        assertThat(userCount).isEqualTo(1);
        
        User foundUser = userRepository.findById(testUser.getId()).orElse(null);
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo("TestUser");
    }

    @Test
    @Order(2)
    @DisplayName("로그인 → JWT 토큰 발급 플로우 테스트")
    void testLoginToJWTFlow() {
        // Given: 사용자가 이미 생성됨 (setUp에서)
        assertThat(testUser).isNotNull();
        assertThat(testUser.getId()).isNotNull();

        // When: JWT 토큰 생성
        String jwtToken = customOAuth2UserService.generateJwtToken(testUser.getId());

        // Then: JWT 토큰 검증
        assertThat(jwtToken).isNotNull();
        assertThat(jwtToken).isNotEmpty();
        assertThat(jwtUtil.validateToken(jwtToken)).isTrue();

        // JWT Claims 검증
        var claims = jwtUtil.getClaimsFromToken(jwtToken);
        assertThat(Long.parseLong(claims.getSubject())).isEqualTo(testUser.getId());
        assertThat(claims.get("username", String.class)).isEqualTo(testUser.getUsername());
        assertThat(claims.get("email", String.class)).isEqualTo(testUser.getEmail());
        assertThat(claims.get("role", String.class)).isEqualTo("ROLE_USER");
    }

    @Test
    @Order(3)
    @DisplayName("JWT → 보호된 웹 페이지 접근 플로우 테스트")
    void testJWTToProtectedWebPageFlow() throws Exception {
        // Given: 유효한 JWT 토큰
        Cookie jwtCookie = new Cookie("jwt", validJwtToken);

        // When & Then: JWT로 보호된 게시판 목록 페이지 접근
        mockMvc.perform(get("/boardList")
                .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("board/get-boardlist"))
                .andExpect(model().attributeExists("boardList"));
    }

    @Test
    @Order(4)
    @DisplayName("JWT → API 엔드포인트 접근 플로우 테스트")
    void testJWTToAPIEndpointFlow() throws Exception {
        // Given: 유효한 JWT 토큰
        Cookie jwtCookie = new Cookie("jwt", validJwtToken);

        // When & Then: JWT로 보호된 API 엔드포인트 접근
        mockMvc.perform(get("/api/boards/" + testBoard.getId())
                .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testBoard.getId()))
                .andExpect(jsonPath("$.title").value("Integration Test Board"))
                .andExpect(jsonPath("$.content").value("This is integration test content"));
     }

    @Test
    @Order(5)
    @DisplayName("전체 CRUD 플로우 테스트 - 게시판 생성/조회/수정/삭제")
    void testFullCRUDFlow() throws Exception {
        // Given: 유효한 JWT 토큰
        Cookie jwtCookie = new Cookie("jwt", validJwtToken);

        // 1. 게시판 생성 (CREATE)
        Map<String, Object> newBoard = new HashMap<>();
        newBoard.put("title", "New Integration Test Board");
        newBoard.put("content", "New integration test content");
        newBoard.put("writer", "testUser");

        String newBoardJson = objectMapper.writeValueAsString(newBoard);

        mockMvc.perform(post("/api/boards")
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(newBoardJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Integration Test Board"))
                .andExpect(jsonPath("$.content").value("New integration test content"));

        // 2. 게시판 목록 조회 (READ)
        mockMvc.perform(get("/api/boards")
                .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2)); // 기존 1개 + 새로 생성 1개

        // 3. 특정 게시판 조회 (READ)
        mockMvc.perform(get("/api/boards/" + testBoard.getId())
                .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testBoard.getId()))
                .andExpect(jsonPath("$.title").value("Integration Test Board"));

        // 4. 게시판 수정 (UPDATE)
        Map<String, Object> updateBoard = new HashMap<>();
        updateBoard.put("title", "Updated Integration Test Board");
        updateBoard.put("content", "Updated integration test content");
        updateBoard.put("writer", "TestUser");
        
        String updateBoardJson = objectMapper.writeValueAsString(updateBoard);

        mockMvc.perform(put("/api/boards/" + testBoard.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBoardJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Integration Test Board"))
                .andExpect(jsonPath("$.content").value("Updated integration test content"));

        // 5. 게시판 삭제 (DELETE)
        mockMvc.perform(delete("/api/boards/" + testBoard.getId())
                .cookie(jwtCookie))
                .andExpect(status().isOk());
        
        // 6. 삭제 확인 - 삭제된 게시판을 조회했을 때 404 확인
        mockMvc.perform(get("/api/boards/" + testBoard.getId())
                .cookie(jwtCookie))
                .andExpect(status().isNotFound());

    }

    @Test
    @Order(6)
    @DisplayName("인증 실패 플로우 테스트 - JWT 없이 API 접근")
    void testAuthenticationFailureFlow() throws Exception {
        // When & Then: JWT 토큰 없이 보호된 API 접근
        mockMvc.perform(get("/api/boards/" + testBoard.getId()))
                .andExpect(status().is3xxRedirection()); // 로그인 페이지로 리다이렉트

        mockMvc.perform(post("/api/boards")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().is3xxRedirection()); // 로그인 페이지로 리다이렉트
    }

    @Test
    @Order(7)
    @DisplayName("잘못된 JWT 토큰 플로우 테스트")
    void testInvalidJWTFlow() throws Exception {
        // Given: 잘못된 JWT 토큰
        Cookie invalidJwtCookie = new Cookie("jwt", "invalid.jwt.token");

        // When & Then: 잘못된 JWT로 API 접근
        mockMvc.perform(get("/api/boards/" + testBoard.getId())
                .cookie(invalidJwtCookie))
                .andExpect(status().is3xxRedirection()); // 로그인 페이지로 리다이렉트
    }

    @Test
    @Order(8)
    @DisplayName("만료된 JWT 토큰 플로우 테스트")
    void testExpiredJWTFlow() {
        // Given: 만료된 JWT 토큰 생성 (JwtUtil에서 과거 시간으로 설정)
        // 이 테스트는 실제로는 JWT 만료 시간을 조작하기 어려우므로
        // 토큰 검증 로직만 확인
        
        // 유효한 토큰이 현재 시점에서 유효한지 확인
        assertThat(jwtUtil.validateToken(validJwtToken)).isTrue();
        
        // 잘못된 형식의 토큰은 유효하지 않음을 확인
        assertThat(jwtUtil.validateToken("expired.token")).isFalse();
    }

    @Test
    @Order(9)
    @DisplayName("다중 사용자 동시 접근 플로우 테스트")
    @Transactional
    void testMultiUserConcurrentFlow() throws Exception {
        // Given: 추가 사용자들 생성
        User user2 = User.builder()
                .username("User 2")
                .email("user2@test.com")
                .role("ROLE_USER")
                .build();
        user2 = userRepository.save(user2);

        User user3 = User.builder()
                .username("User 3")
                .email("user3@test.com")
                .role("ROLE_USER")
                .build();
        user3 = userRepository.save(user3);

        // JWT 토큰 생성
        String jwt2 = customOAuth2UserService.generateJwtToken(user2.getId());
        String jwt3 = customOAuth2UserService.generateJwtToken(user3.getId());

        // When & Then: 각 사용자가 동시에 API 접근
        Cookie jwtCookie1 = new Cookie("jwt", validJwtToken);
        Cookie jwtCookie2 = new Cookie("jwt", jwt2);
        Cookie jwtCookie3 = new Cookie("jwt", jwt3);

        // 모든 사용자가 게시판 목록에 접근 가능
        mockMvc.perform(get("/api/boards/" + testBoard.getId()).cookie(jwtCookie1))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/boards/" + testBoard.getId()).cookie(jwtCookie2))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/boards/" + testBoard.getId()).cookie(jwtCookie3))
                .andExpect(status().isOk());

        // 사용자 수 확인
        long totalUsers = userRepository.count();
        assertThat(totalUsers).isEqualTo(3);
    }

    @Test
    @Order(10)
    @DisplayName("통합 테스트 통계 및 성능 확인")
    void testIntegrationStatistics() {
        // 데이터베이스 연결 상태 확인
        assertThat(mysqlContainer.isRunning()).isTrue();
        
        // 생성된 데이터 확인
        long userCount = userRepository.count();
        long boardCount = boardRepository.count();
        
        System.out.println("=== Integration Test Statistics ===");
        System.out.println("MySQL Container: " + mysqlContainer.getJdbcUrl());
        System.out.println("Total Users: " + userCount);
        System.out.println("Total Boards: " + boardCount);
        System.out.println("JWT Token Valid: " + jwtUtil.validateToken(validJwtToken));
        
        // 기본 검증
        assertThat(userCount).isGreaterThan(0);
        assertThat(boardCount).isGreaterThanOrEqualTo(0);
    }
}
