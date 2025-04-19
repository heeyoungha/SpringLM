package com.example.springlm.board.reply;

import com.example.springlm.board.Board;
import com.example.springlm.board.BoardRepository;
import com.example.springlm.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.springlm.common.ServiceUtil;
import com.example.springlm.common.exception.DomainException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final BoardRepository boardRepository;

    @Transactional
    public List<Reply> cerateReply(ReplyDto replyDto, User user, Long boardId) {
        Board board = ServiceUtil.findByIdOrThrow(boardRepository, boardId, DomainException.notFindRow(boardId));
        Reply reply = Reply.builder()
                .board(board)
                .user(user)
                .content(replyDto.getContent())
                .build();
        replyRepository.save(reply);
        return board.getReplyList();
    }

    @Transactional(readOnly = true)
    public Reply getReply(Long replyId) {
        return ServiceUtil.findByIdOrThrow(replyRepository, replyId, DomainException.notFindRow(replyId));
    }

    @Transactional
    public ReplyDto updateReply(Long replyId, ReplyDto replyDto) {
        Reply reply = ServiceUtil.findByIdOrThrow(replyRepository, replyId, DomainException.notFindRow(replyId));
        reply.updateReply(replyDto.getContent());
        return new ReplyDto(reply);
    }

    @Transactional(readOnly = true)
    public List<Reply> getReplyList(Long boardId) {
        Board board = ServiceUtil.findByIdOrThrow(boardRepository, boardId, DomainException.notFindRow(boardId));
        return board.getReplyList();
    }

    @Transactional
    public void deleteReply(Long replyId) {
        Reply reply = ServiceUtil.findByIdOrThrow(replyRepository, replyId, DomainException.notFindRow(replyId));
        replyRepository.delete(reply);
    }
}
