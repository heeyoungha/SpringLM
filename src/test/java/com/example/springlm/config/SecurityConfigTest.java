package com.example.springlm.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("SecurityConfig 테스트")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("공개 엔드포인트 접근 테스트 - 로그인 페이지")
    void publicEndpoint_LoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login/login"));
    }

    @Test
    @DisplayName("보호된 엔드포인트 접근 테스트 - 게시판 목록 (비인증)")
    void protectedEndpoint_BoardList_Unauthenticated() throws Exception {
        mockMvc.perform(get("/boardList"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("보호된 엔드포인트 접근 테스트 - 게시판 목록 (인증됨)")
    @WithMockUser(username = "testuser", roles = "USER")
    void protectedEndpoint_BoardList_Authenticated() throws Exception {
        mockMvc.perform(get("/boardList"))
                .andExpect(status().isOk())
                .andExpect(view().name("board/get-boardlist"));
    }

    @Test
    @DisplayName("OAuth2 로그인 엔드포인트 접근 테스트")
    void oauth2LoginEndpoint() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().is3xxRedirection());
    }
}
