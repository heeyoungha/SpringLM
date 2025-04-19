package com.example.springlm.board;

import com.example.springlm.common.BaseEntity;
import com.example.springlm.board.reply.Reply;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.List;

@Getter
@Entity
@Table(name = "board")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Where(clause = "is_deleted = false")
@SQLDelete(sql = "UPDATE board SET is_deleted = true WHERE board_id = ?")
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long id;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 10, nullable = false)
    private String writer;

    @Column(length = 10, nullable= true)
    private String tag;

    @OneToMany(mappedBy = "board", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<Reply> replyList;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // 시드 데이터용 생성자 추가 (tag 포함)
    public Board(String title, String content, String writer, String tag) {
        this.title = title;
        this.content = content;
        this.writer = writer;
        this.tag = tag;
    }

    @Builder(toBuilder = true)
    public Board(Long id, String title, String content, String writer){
        this.id = id;
        this.title = title;
        this.content = content;
        this.writer = writer;
    }


}

