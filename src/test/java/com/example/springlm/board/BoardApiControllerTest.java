package com.example.springlm.board;

import com.example.springlm.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("BoardApiController 테스트")
class BoardApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BoardService boardService;

    private User testUser;
    private BoardDto testBoardDto;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role("USER")
                .build();

        // 테스트용 BoardDto 생성
        testBoardDto = new BoardDto();
        testBoardDto.setId(1L);
        testBoardDto.setWriter("testuser");
        testBoardDto.setTitle("테스트 게시글");
        testBoardDto.setContent("테스트 내용");
        testBoardDto.setCreatedDate(LocalDateTime.now());
    }

    @Test
    @DisplayName("게시판 생성 API 테스트 - 인증된 사용자")
    @WithMockUser(username = "testuser", roles = "USER")
    void createBoard_AuthenticatedUser() throws Exception {
        // given
        when(boardService.saveBoard(any(BoardDto.class))).thenReturn(testBoardDto);

        String requestBody = """
                {
                    "title": "테스트 게시글",
                    "content": "테스트 내용",
                    "writer": "testuser"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/board")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("테스트 게시글"))
                .andExpect(jsonPath("$.content").value("테스트 내용"))
                .andExpect(jsonPath("$.writer").value("testuser"));
    }

    @Test
    @DisplayName("게시판 생성 API 테스트 - 비인증 사용자")
    void createBoard_UnauthenticatedUser() throws Exception {
        String requestBody = """
                {
                    "title": "테스트 게시글",
                    "content": "테스트 내용",
                    "writer": "testuser"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/board")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
