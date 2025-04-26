package com.example.springlm.config;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT 유틸리티 테스트")
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    private Long testUserId;
    private String testUsername;
    private String testEmail;
    private String testRole;

    @BeforeEach
    void setUp() {
        // JwtUtil의 private 필드에 테스트 값 설정 (HS512 알고리즘용 최소 64바이트 키)
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", "test_jwt_secret_key_for_testing_minimum_64_bytes_length_required_for_HS512_algorithm_security");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 3600000);
        
        testUserId = 1L;
        testUsername = "testuser";
        testEmail = "test@example.com";
        testRole = "USER";
    }

    @Test
    @DisplayName("유효한 JWT 토큰 생성 테스트")
    void generateToken_ShouldReturnValidToken() {
        // When
        String token = jwtUtil.generateToken(testUserId, testUsername, testEmail, testRole);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT는 3개 부분으로 구성
    }

    @Test
    @DisplayName("유효한 JWT 토큰 검증 성공 테스트")
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Given
        String token = jwtUtil.generateToken(testUserId, testUsername, testEmail, testRole);

        // When
        boolean isValid = jwtUtil.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("잘못된 JWT 토큰 검증 실패 테스트")
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When
        boolean isValid = jwtUtil.validateToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("null 토큰 검증 실패 테스트")
    void validateToken_WithNullToken_ShouldReturnFalse() {
        // When
        boolean isValid = jwtUtil.validateToken(null);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("빈 문자열 토큰 검증 실패 테스트")
    void validateToken_WithEmptyToken_ShouldReturnFalse() {
        // When
        boolean isValid = jwtUtil.validateToken("");

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("JWT 토큰에서 Claims 추출 테스트")
    void getClaimsFromToken_ShouldReturnCorrectClaims() {
        // Given
        String token = jwtUtil.generateToken(testUserId, testUsername, testEmail, testRole);

        // When
        Claims claims = jwtUtil.getClaimsFromToken(token);

        // Then
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo(testUserId.toString());
        assertThat(claims.get("username", String.class)).isEqualTo(testUsername);
        assertThat(claims.get("email", String.class)).isEqualTo(testEmail);
        assertThat(claims.get("role", String.class)).isEqualTo(testRole);
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
    }

    @Test
    @DisplayName("JWT 토큰에서 사용자 ID 추출 테스트")
    void getUserIdFromToken_ShouldReturnCorrectUserId() {
        // Given
        String token = jwtUtil.generateToken(testUserId, testUsername, testEmail, testRole);

        // When
        Long extractedUserId = jwtUtil.getUserIdFromToken(token);

        // Then
        assertThat(extractedUserId).isEqualTo(testUserId);
    }

    @Test
    @DisplayName("JWT 토큰에서 사용자명 추출 테스트")
    void getUsernameFromToken_ShouldReturnCorrectUsername() {
        // Given
        String token = jwtUtil.generateToken(testUserId, testUsername, testEmail, testRole);

        // When
        String extractedUsername = jwtUtil.getUsernameFromToken(token);

        // Then
        assertThat(extractedUsername).isEqualTo(testUsername);
    }

    @Test
    @DisplayName("새로 생성된 JWT 토큰은 만료되지 않음 테스트")
    void isTokenExpired_WithNewToken_ShouldReturnFalse() {
        // Given
        String token = jwtUtil.generateToken(testUserId, testUsername, testEmail, testRole);

        // When
        boolean isExpired = jwtUtil.isTokenExpired(token);

        // Then
        assertThat(isExpired).isFalse();
    }

    @Test
    @DisplayName("잘못된 토큰은 만료된 것으로 처리 테스트")
    void isTokenExpired_WithInvalidToken_ShouldReturnTrue() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When
        boolean isExpired = jwtUtil.isTokenExpired(invalidToken);

        // Then
        assertThat(isExpired).isTrue();
    }

    @Test
    @DisplayName("여러 사용자의 토큰이 서로 다른 정보를 가짐 테스트")
    void generateToken_ForDifferentUsers_ShouldHaveDifferentClaims() {
        // Given
        Long userId1 = 1L, userId2 = 2L;
        String username1 = "user1", username2 = "user2";
        String email1 = "user1@test.com", email2 = "user2@test.com";

        // When
        String token1 = jwtUtil.generateToken(userId1, username1, email1, testRole);
        String token2 = jwtUtil.generateToken(userId2, username2, email2, testRole);

        // Then
        assertThat(token1).isNotEqualTo(token2);
        
        Claims claims1 = jwtUtil.getClaimsFromToken(token1);
        Claims claims2 = jwtUtil.getClaimsFromToken(token2);
        
        assertThat(claims1.getSubject()).isEqualTo(userId1.toString());
        assertThat(claims2.getSubject()).isEqualTo(userId2.toString());
        assertThat(claims1.get("username")).isEqualTo(username1);
        assertThat(claims2.get("username")).isEqualTo(username2);
    }
} 