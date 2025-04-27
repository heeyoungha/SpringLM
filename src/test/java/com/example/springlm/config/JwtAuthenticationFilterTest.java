package com.example.springlm.config;

import com.example.springlm.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("JwtAuthenticationFilter 테스트")
class JwtAuthenticationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    private User testUser;
    private String validToken;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role("USER")
                .build();

        // 유효한 JWT 토큰 생성
        validToken = jwtUtil.generateToken(testUser.getId(), testUser.getUsername(), testUser.getEmail(), testUser.getRole());
    }

    @Test
    @DisplayName("유효한 JWT 토큰으로 인증 성공 테스트")
    void validJwtToken_AuthenticationSuccess() throws Exception {
        mockMvc.perform(get("/boardList")
                        .cookie(new jakarta.servlet.http.Cookie("jwt", validToken)))
                .andExpect(status().isOk())
                .andExpect(view().name("board/get-boardlist"));
    }

    @Test
    @DisplayName("잘못된 JWT 토큰으로 인증 실패 테스트")
    void invalidJwtToken_AuthenticationFailure() throws Exception {
        String invalidToken = "invalid.jwt.token";
        
        mockMvc.perform(get("/boardList")
                        .cookie(new jakarta.servlet.http.Cookie("jwt", invalidToken)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("JWT 토큰 없이 접근 시 인증 실패 테스트")
    void noJwtToken_AuthenticationFailure() throws Exception {
        mockMvc.perform(get("/boardList"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
