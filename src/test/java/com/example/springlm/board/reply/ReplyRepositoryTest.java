package com.example.springlm.board.reply;

import com.example.springlm.board.Board;
import com.example.springlm.board.BoardRepository;
import com.example.springlm.user.User;
import com.example.springlm.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ReplyRepository 테스트")
class ReplyRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReplyRepository replyRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Board testBoard;

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

        // 테스트용 게시판 생성
        testBoard = new Board("테스트 게시판", "테스트 내용", "testuser", "공지");
        testBoard = boardRepository.save(testBoard);
        
        entityManager.flush();
    }

    @Test
    @DisplayName("댓글 저장 및 조회 테스트")
    void saveAndFindReply() {
        // given
        Reply reply = Reply.builder()
                .board(testBoard)
                .user(testUser)
                .content("테스트 댓글입니다.")
                .build();

        // when
        Reply savedReply = replyRepository.save(reply);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<Reply> foundReply = replyRepository.findById(savedReply.getId());
        assertThat(foundReply).isPresent();
        assertThat(foundReply.get().getContent()).isEqualTo("테스트 댓글입니다.");
        assertThat(foundReply.get().getBoard().getId()).isEqualTo(testBoard.getId());
        assertThat(foundReply.get().getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("게시판에 여러 댓글 저장 테스트")
    void saveMultipleReplies() {
        // given
        Reply reply1 = Reply.builder()
                .board(testBoard)
                .user(testUser)
                .content("첫 번째 댓글")
                .build();
        
        Reply reply2 = Reply.builder()
                .board(testBoard)
                .user(testUser)
                .content("두 번째 댓글")
                .build();

        // when
        List<Reply> savedReplies = replyRepository.saveAll(List.of(reply1, reply2));
        entityManager.flush();
        entityManager.clear();

        // then
        List<Reply> allReplies = replyRepository.findAll();
        assertThat(allReplies).hasSize(2);
        assertThat(allReplies)
                .extracting(Reply::getContent)
                .containsExactlyInAnyOrder("첫 번째 댓글", "두 번째 댓글");
    }

    @Test
    @DisplayName("댓글 수정 테스트")
    void updateReply() {
        // given
        Reply reply = Reply.builder()
                .board(testBoard)
                .user(testUser)
                .content("원래 댓글")
                .build();
        Reply savedReply = replyRepository.save(reply);
        entityManager.flush();
        entityManager.clear();

        // when
        Reply foundReply = replyRepository.findById(savedReply.getId()).orElseThrow();
        foundReply.updateReply("수정된 댓글");
        replyRepository.save(foundReply);
        entityManager.flush();
        entityManager.clear();

        // then
        Reply updatedReply = replyRepository.findById(savedReply.getId()).orElseThrow();
        assertThat(updatedReply.getContent()).isEqualTo("수정된 댓글");
    }

    @Test
    @DisplayName("댓글 삭제 테스트")
    void deleteReply() {
        // given
        Reply reply = Reply.builder()
                .board(testBoard)
                .user(testUser)
                .content("삭제할 댓글")
                .build();
        Reply savedReply = replyRepository.save(reply);
        entityManager.flush();
        entityManager.clear();

        // when
        replyRepository.deleteById(savedReply.getId());
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<Reply> foundReply = replyRepository.findById(savedReply.getId());
        assertThat(foundReply).isEmpty();
    }

    @Test
    @DisplayName("게시판과 댓글 연관관계 테스트")
    void boardReplyRelationship() {
        // given
        Reply reply1 = Reply.builder()
                .board(testBoard)
                .user(testUser)
                .content("댓글 1")
                .build();
        
        Reply reply2 = Reply.builder()
                .board(testBoard)
                .user(testUser)
                .content("댓글 2")
                .build();
        
        replyRepository.saveAll(List.of(reply1, reply2));
        entityManager.flush();
        entityManager.clear();

        // when
        Board foundBoard = boardRepository.findById(testBoard.getId()).orElseThrow();

        // then
        assertThat(foundBoard.getReplyList()).hasSize(2);
        assertThat(foundBoard.getReplyList())
                .extracting(Reply::getContent)
                .containsExactlyInAnyOrder("댓글 1", "댓글 2");
    }

    @Test
    @DisplayName("사용자와 댓글 연관관계 테스트")
    void userReplyRelationship() {
        // given
        User anotherUser = User.builder()
                .username("anotheruser")
                .email("another@example.com")
                .role("USER")
                .pw("password456")
                .build();
        anotherUser = userRepository.save(anotherUser);

        Reply reply1 = Reply.builder()
                .board(testBoard)
                .user(testUser)
                .content("첫 번째 사용자 댓글")
                .build();
        
        Reply reply2 = Reply.builder()
                .board(testBoard)
                .user(anotherUser)
                .content("두 번째 사용자 댓글")
                .build();
        
        replyRepository.saveAll(List.of(reply1, reply2));
        entityManager.flush();
        entityManager.clear();

        // when
        List<Reply> allReplies = replyRepository.findAll();

        // then
        assertThat(allReplies).hasSize(2);
        assertThat(allReplies)
                .extracting(reply -> reply.getUser().getUsername())
                .containsExactlyInAnyOrder("testuser", "anotheruser");
    }

    @Test
    @DisplayName("게시판 삭제 시 댓글도 함께 삭제되는지 테스트 (Cascade)")
    void boardDeletionCascadeToReplies() {
        // given
        Reply reply = Reply.builder()
                .board(testBoard)
                .user(testUser)
                .content("게시판과 함께 삭제될 댓글")
                .build();
        replyRepository.save(reply);
        entityManager.flush();
        entityManager.clear();

        // when - 게시판 삭제
        boardRepository.deleteById(testBoard.getId());
        entityManager.flush();
        entityManager.clear();

        // then - 댓글도 함께 삭제되어야 함 (orphanRemoval = true)
        List<Reply> remainingReplies = replyRepository.findAll();
        assertThat(remainingReplies).isEmpty();
    }

    @Test
    @DisplayName("전체 댓글 조회 테스트")
    void findAllReplies() {
        // given
        Board anotherBoard = new Board("다른 게시판", "내용", "testuser", "일반");
        anotherBoard = boardRepository.save(anotherBoard);

        Reply reply1 = Reply.builder()
                .board(testBoard)
                .user(testUser)
                .content("첫 번째 게시판 댓글")
                .build();
        
        Reply reply2 = Reply.builder()
                .board(anotherBoard)
                .user(testUser)
                .content("두 번째 게시판 댓글")
                .build();
        
        replyRepository.saveAll(List.of(reply1, reply2));
        entityManager.flush();
        entityManager.clear();

        // when
        List<Reply> allReplies = replyRepository.findAll();

        // then
        assertThat(allReplies).hasSize(2);
        assertThat(allReplies)
                .extracting(Reply::getContent)
                .containsExactlyInAnyOrder("첫 번째 게시판 댓글", "두 번째 게시판 댓글");
    }

    @Test
    @DisplayName("댓글 내용 길이 제한 테스트")
    void replyContentLengthLimit() {
        // given - 120자 제한
        String longContent = "a".repeat(120); // 정확히 120자
        Reply reply = Reply.builder()
                .board(testBoard)
                .user(testUser)
                .content(longContent)
                .build();

        // when
        Reply savedReply = replyRepository.save(reply);
        entityManager.flush();
        entityManager.clear();

        // then
        Reply foundReply = replyRepository.findById(savedReply.getId()).orElseThrow();
        assertThat(foundReply.getContent()).hasSize(120);
        assertThat(foundReply.getContent()).isEqualTo(longContent);
    }
}
