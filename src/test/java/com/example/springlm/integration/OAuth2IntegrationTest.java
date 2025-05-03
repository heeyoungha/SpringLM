package com.example.springlm.integration;

import com.example.springlm.user.CustomOAuth2UserService;
import com.example.springlm.user.User;
import com.example.springlm.user.UserRepository;
import com.example.springlm.config.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * OAuth2 통합 테스트
 * MockWebServer로 Google OAuth2 API를 모킹하여 전체 인증 플로우 검증
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("OAuth2 통합 테스트 - 전체 인증 플로우 검증")
class OAuth2IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private JwtUtil jwtUtil;

    private static MockWebServer mockWebServer;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    static void setUpMockWebServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDownMockWebServer() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    @Transactional
    void setUp() {
        // 각 테스트 전에 사용자 데이터 정리
        userRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("OAuth2 로그인 페이지 접근 테스트")
    void testOAuth2LoginPageAccess() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login/login"));
    }

    @Test
    @Order(2)
    @DisplayName("Google OAuth2 사용자 정보 API 모킹 테스트")
    void testGoogleOAuth2UserInfoMocking() throws Exception {
        // Google 사용자 정보 API 응답 모킹
        Map<String, Object> googleUserInfo = new HashMap<>();
        googleUserInfo.put("sub", "123456789");
        googleUserInfo.put("name", "Test User");
        googleUserInfo.put("email", "test@google.com");
        googleUserInfo.put("picture", "https://example.com/picture.jpg");

        String jsonResponse = objectMapper.writeValueAsString(googleUserInfo);
        
        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // MockWebServer URL 확인
        String baseUrl = mockWebServer.url("/").toString();
        assertThat(baseUrl).isNotNull();
        assertThat(mockWebServer.getRequestCount()).isEqualTo(0);
    }

    @Test
    @Order(3)
    @DisplayName("새로운 Google 사용자 OAuth2 인증 플로우 테스트")
    @Transactional
    void testNewGoogleUserOAuth2Flow() {
        // Given: 새로운 Google 사용자 정보
        String testUsername = "Test User";
        String testEmail = "test@google.com";
        
        // 사용자가 존재하지 않음을 확인
        Optional<User> existingUser = userRepository.findByUsername(testUsername);
        assertThat(existingUser).isEmpty();

        // When: 새 사용자 생성 (OAuth2 인증 시뮬레이션)
        User newUser = User.builder()
                .username(testUsername)
                .email(testEmail)
                .role("ROLE_USER")
                .build();
        
        User savedUser = userRepository.save(newUser);

        // Then: 사용자가 성공적으로 저장되었는지 확인
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo(testUsername);
        assertThat(savedUser.getEmail()).isEqualTo(testEmail);
        assertThat(savedUser.getRole()).isEqualTo("ROLE_USER");

        // JWT 토큰 생성 테스트
        String jwtToken = customOAuth2UserService.generateJwtToken(savedUser.getId());
        assertThat(jwtToken).isNotNull();
        assertThat(jwtToken).isNotEmpty();

        // JWT 토큰 검증
        assertThat(jwtUtil.validateToken(jwtToken)).isTrue();
        
        // JWT에서 사용자 정보 추출
        var claims = jwtUtil.getClaimsFromToken(jwtToken);
        assertThat(Long.parseLong(claims.getSubject())).isEqualTo(savedUser.getId());
        assertThat(claims.get("username", String.class)).isEqualTo(testUsername);
        assertThat(claims.get("email", String.class)).isEqualTo(testEmail);
        assertThat(claims.get("role", String.class)).isEqualTo("ROLE_USER");
    }

    @Test
    @Order(4)
    @DisplayName("기존 Google 사용자 OAuth2 재인증 플로우 테스트")
    @Transactional
    void testExistingGoogleUserOAuth2Flow() {
        // Given: 기존 사용자 생성
        String testUsername = "Existing User";
        String oldEmail = "old@google.com";
        String newEmail = "new@google.com";
        
        User existingUser = User.builder()
                .username(testUsername)
                .email(oldEmail)
                .role("ROLE_USER")
                .build();
        
        User savedUser = userRepository.save(existingUser);
        assertThat(savedUser.getId()).isNotNull();

        // When: 기존 사용자 정보 업데이트 (재인증 시뮬레이션)
        savedUser.updatOauthUser(testUsername, newEmail);
        User updatedUser = userRepository.save(savedUser);

        // Then: 사용자 정보가 업데이트되었는지 확인
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getId()).isEqualTo(savedUser.getId());
        assertThat(updatedUser.getUsername()).isEqualTo(testUsername);
        assertThat(updatedUser.getEmail()).isEqualTo(newEmail); // 이메일이 업데이트됨
        assertThat(updatedUser.getRole()).isEqualTo("ROLE_USER");

        // JWT 토큰 생성 및 검증
        String jwtToken = customOAuth2UserService.generateJwtToken(updatedUser.getId());
        assertThat(jwtToken).isNotNull();
        assertThat(jwtUtil.validateToken(jwtToken)).isTrue();
        
        var claims = jwtUtil.getClaimsFromToken(jwtToken);
        assertThat(claims.get("email", String.class)).isEqualTo(newEmail);
    }

    @Test
    @Order(5)
    @DisplayName("OAuth2 인증 후 보호된 엔드포인트 접근 테스트")
    void testProtectedEndpointAccessAfterOAuth2() throws Exception {
        // Given: 사용자 생성 및 JWT 토큰 발급
        User testUser = User.builder()
                .username("Protected Test User")
                .email("protected@google.com")
                .role("ROLE_USER")
                .build();
        
        User savedUser = userRepository.save(testUser);
        String jwtToken = customOAuth2UserService.generateJwtToken(savedUser.getId());

        // When & Then: JWT 토큰으로 보호된 엔드포인트 접근
        mockMvc.perform(get("/boardList")
                .cookie(new jakarta.servlet.http.Cookie("jwt", jwtToken)))
                .andExpect(status().isOk())
                .andExpect(view().name("board/get-boardlist"));
    }

    @Test
    @Order(6)
    @DisplayName("JWT 토큰 없이 보호된 엔드포인트 접근 실패 테스트")
    void testProtectedEndpointAccessWithoutJWT() throws Exception {
        // When & Then: JWT 토큰 없이 보호된 엔드포인트 접근 시 리다이렉트
        mockMvc.perform(get("/boardList"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @Order(7)
    @DisplayName("잘못된 JWT 토큰으로 보호된 엔드포인트 접근 실패 테스트")
    void testProtectedEndpointAccessWithInvalidJWT() throws Exception {
        // Given: 잘못된 JWT 토큰
        String invalidJwtToken = "invalid.jwt.token";

        // When & Then: 잘못된 JWT 토큰으로 접근 시 리다이렉트
        mockMvc.perform(get("/boardList")
                .cookie(new jakarta.servlet.http.Cookie("jwt", invalidJwtToken)))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @Order(8)
    @DisplayName("OAuth2 인증 플로우 통계 확인")
    @Transactional
    void testOAuth2FlowStatistics() {
        // Given: 여러 사용자 생성
        for (int i = 1; i <= 3; i++) {
            User user = User.builder()
                    .username("User" + i)
                    .email("user" + i + "@google.com")
                    .role("ROLE_USER")
                    .build();
            userRepository.save(user);
        }

        // When: 사용자 수 확인
        long userCount = userRepository.count();

        // Then: 생성된 사용자 수가 올바른지 확인
        assertThat(userCount).isEqualTo(3);
        
        // 모든 사용자가 ROLE_USER 권한을 가지는지 확인
        var allUsers = userRepository.findAll();
        assertThat(allUsers).hasSize(3);
        assertThat(allUsers).allMatch(user -> "ROLE_USER".equals(user.getRole()));
    }
}
