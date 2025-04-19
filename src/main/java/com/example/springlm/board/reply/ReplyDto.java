package com.example.springlm.board.reply;

import com.example.springlm.board.Board;
import com.example.springlm.user.User;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ReplyDto {

    private Long replyId;

    private Long boardId;

    private String content;
    private String username;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    // 실제 사용되지 않음 - ReplyService에서 직접 Reply.builder() 사용
    /*
    public Reply toEntity(Board board, User user){
        Reply build = Reply.builder()
                .content(this.content)
                .user(user)
                .board(board)
                .build();
        return build;
    }
    */

    @Builder
    public ReplyDto(Reply reply){
        this.replyId = reply.getId();
        this.content = reply.getContent();
        this.createdDate = reply.getCreatedAt();
        this.modifiedDate = reply.getUpdatedAt();
    }

}