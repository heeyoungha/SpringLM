package com.example.springlm.board;

import com.example.springlm.user.User;
import com.example.springlm.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("BoardRepository 테스트")
class BoardRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .role("USER")
                .pw("password123")
                .build();
        testUser = userRepository.save(testUser);
        entityManager.flush();
    }

    @Test
    @DisplayName("게시판 저장 및 조회 테스트")
    void saveAndFindBoard() {
        // given
        Board board = new Board("테스트 제목", "테스트 내용", "testuser", "공지");

        // when
        Board savedBoard = boardRepository.save(board);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<Board> foundBoard = boardRepository.findById(savedBoard.getId());
        assertThat(foundBoard).isPresent();
        assertThat(foundBoard.get().getTitle()).isEqualTo("테스트 제목");
        assertThat(foundBoard.get().getContent()).isEqualTo("테스트 내용");
        assertThat(foundBoard.get().getWriter()).isEqualTo("testuser");
        assertThat(foundBoard.get().getTag()).isEqualTo("공지");
    }

    @Test
    @DisplayName("제목으로 게시판 검색 테스트")
    void findByTitleContaining() {
        // given
        Board board1 = new Board("Spring Boot 테스트", "내용1", "testuser", "공지");
        Board board2 = new Board("JPA 테스트", "내용2", "testuser", "일반");
        Board board3 = new Board("Spring Security", "내용3", "testuser", "공지");
        
        boardRepository.saveAll(List.of(board1, board2, board3));
        entityManager.flush();
        entityManager.clear();

        // when
        Pageable pageable = PageRequest.of(0, 10);
        Page<Board> springBoards = boardRepository.findByTitleContaining("Spring", pageable);

        // then
        assertThat(springBoards.getContent()).hasSize(2);
        assertThat(springBoards.getContent())
                .extracting(Board::getTitle)
                .containsExactlyInAnyOrder("Spring Boot 테스트", "Spring Security");
    }

    @Test
    @DisplayName("제목 검색 - 정확한 대소문자로 검색")
    void findByTitleContaining_ExactCase() {
        // given
        Board board1 = new Board("Spring Boot", "내용1", "testuser", "공지");
        Board board2 = new Board("spring framework", "내용2", "testuser", "일반");
        Board board3 = new Board("SPRING SECURITY", "내용3", "testuser", "공지");
        
        boardRepository.saveAll(List.of(board1, board2, board3));
        entityManager.flush();
        entityManager.clear();

        // when - 정확한 대소문자로 검색
        Pageable pageable = PageRequest.of(0, 10);
        Page<Board> springBoards = boardRepository.findByTitleContaining("Spring", pageable);

        // then - H2는 기본적으로 대소문자를 구분하므로 정확히 일치하는 것만 찾음
        assertThat(springBoards.getContent()).hasSize(1);
        assertThat(springBoards.getContent())
                .extracting(Board::getTitle)
                .containsExactly("Spring Boot");
    }

    @Test
    @DisplayName("제목 검색 - 빈 결과 반환")
    void findByTitleContaining_EmptyResult() {
        // given
        Board board = new Board("JPA 테스트", "내용", "testuser", "공지");
        boardRepository.save(board);
        entityManager.flush();
        entityManager.clear();

        // when
        Pageable pageable = PageRequest.of(0, 10);
        Page<Board> result = boardRepository.findByTitleContaining("Spring", pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("제목 검색 - 페이징 테스트")
    void findByTitleContaining_Pagination() {
        // given
        for (int i = 1; i <= 15; i++) {
            Board board = new Board("Spring Boot " + i, "내용 " + i, "testuser", "공지");
            boardRepository.save(board);
        }
        entityManager.flush();
        entityManager.clear();

        // when - 첫 번째 페이지
        Pageable pageable = PageRequest.of(0, 5);
        Page<Board> firstPage = boardRepository.findByTitleContaining("Spring", pageable);

        // then
        assertThat(firstPage.getContent()).hasSize(5);
        assertThat(firstPage.getTotalElements()).isEqualTo(15);
        assertThat(firstPage.getTotalPages()).isEqualTo(3);
        assertThat(firstPage.hasNext()).isTrue();
        assertThat(firstPage.isFirst()).isTrue();
    }

    @Test
    @DisplayName("게시판 수정 테스트")
    void updateBoard() {
        // given
        Board board = new Board("원래 제목", "원래 내용", "testuser", "공지");
        Board savedBoard = boardRepository.save(board);
        entityManager.flush();
        entityManager.clear();

        // when
        Board foundBoard = boardRepository.findById(savedBoard.getId()).orElseThrow();
        // Board 엔티티에 update 메소드가 없으므로 직접 수정
        foundBoard = foundBoard.toBuilder()
                .title("수정된 제목")
                .content("수정된 내용")
                .build();
        boardRepository.save(foundBoard);
        entityManager.flush();
        entityManager.clear();

        // then
        Board updatedBoard = boardRepository.findById(savedBoard.getId()).orElseThrow();
        assertThat(updatedBoard.getTitle()).isEqualTo("수정된 제목");
        assertThat(updatedBoard.getContent()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("게시판 삭제 테스트 (Soft Delete)")
    void deleteBoard() {
        // given
        Board board = new Board("삭제할 게시판", "내용", "testuser", "공지");
        Board savedBoard = boardRepository.save(board);
        entityManager.flush();
        entityManager.clear();

        // when
        boardRepository.deleteById(savedBoard.getId());
        entityManager.flush();
        entityManager.clear();

        // then (Soft Delete이므로 실제로는 조회되지 않음)
        Optional<Board> foundBoard = boardRepository.findById(savedBoard.getId());
        assertThat(foundBoard).isEmpty();
    }

    @Test
    @DisplayName("게시판과 댓글 연관관계 테스트")
    void boardWithReplies() {
        // given
        Board board = new Board("댓글 테스트", "내용", "testuser", "공지");
        Board savedBoard = boardRepository.save(board);
        entityManager.flush();
        entityManager.clear();

        // when
        Board foundBoard = boardRepository.findById(savedBoard.getId()).orElseThrow();

        // then
        assertThat(foundBoard.getReplyList()).isNotNull();
        assertThat(foundBoard.getReplyList()).isEmpty();
    }

    @Test
    @DisplayName("전체 게시판 조회 테스트")
    void findAllBoards() {
        // given
        Board board1 = new Board("제목1", "내용1", "testuser", "공지");
        Board board2 = new Board("제목2", "내용2", "testuser", "일반");
        Board board3 = new Board("제목3", "내용3", "testuser", "공지");
        
        boardRepository.saveAll(List.of(board1, board2, board3));
        entityManager.flush();
        entityManager.clear();

        // when
        List<Board> allBoards = boardRepository.findAll();

        // then
        assertThat(allBoards).hasSize(3);
        assertThat(allBoards)
                .extracting(Board::getTitle)
                .containsExactlyInAnyOrder("제목1", "제목2", "제목3");
    }
}
