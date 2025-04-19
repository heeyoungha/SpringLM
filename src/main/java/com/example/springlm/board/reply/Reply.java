package com.example.springlm.board.reply;

import com.example.springlm.common.BaseEntity;
import com.example.springlm.board.Board;
import com.example.springlm.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "reply")
@NoArgsConstructor
public class Reply extends BaseEntity {

    @Id
    @Column(name = "reply_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    @Column(nullable = false, length = 120)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="board_id")
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;

    @Builder
    public Reply(Board board, User user, String content){
        this.content = content;
        this.board = board;
        this.user = user;
    }

    public void updateReply(String content){
        this.content = content;
    }

}
