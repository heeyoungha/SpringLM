package com.example.springlm.board.reply;

import com.example.springlm.user.User;
import com.example.springlm.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@RestController
@RequestMapping("/api/board/{boardId}/reply")
@RequiredArgsConstructor
public class ReplyApiController {

    private final ReplyService replyService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<List<Reply>> createReply(@PathVariable("boardId") Long boardId,
                                                   @RequestBody ReplyDto replyDto){
        // 임시 유저
        String username = "testuser";
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.save(createTestUser()));

        List<Reply> replyList = replyService.cerateReply(replyDto, user, boardId);
        return ResponseEntity.ok(replyList);
    }

    @PutMapping("/{replyId}")
    public ResponseEntity<ReplyDto> updateReply(@PathVariable("boardId") Long boardId,
                                             @PathVariable("replyId") Long replyId,
                                             @RequestBody ReplyDto replyDto) {
        ReplyDto updated = replyService.updateReply(replyId, replyDto);
        return ResponseEntity.ok(updated);
    }

    private User createTestUser(){
        User testUser = User.builder()
                .username("testuser")
                .pw("password")
                .email("test@example.com")
                .username("테스트 사용자")
                .build();
        return testUser;
    }
}
