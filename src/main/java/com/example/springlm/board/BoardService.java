package com.example.springlm.board;

import com.example.springlm.board.reply.Reply;
import com.example.springlm.common.ServiceUtil;
import com.example.springlm.common.exception.DomainException;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    // 직접 호출되지 않음 - boardSearchList에서만 내부적으로 사용
    private Page<BoardDto> getBoardList(Pageable pageable) {
        Page<Board> boards = boardRepository.findAll(pageable);
        List<BoardDto> boardDtoList = new ArrayList<>();

        for(Board board : boards) {
            BoardDto dto = BoardDto.builder()
                    .board(board)
                    .build();
            boardDtoList.add(dto);
        }
        return new PageImpl<>(boardDtoList, pageable, boards.getTotalElements());
    }

    public Page<BoardDto> boardSearchList(String searchTitle, Pageable pageable) {
        if(searchTitle == null) {
            return getBoardList(pageable);
        }
        Page<Board> boards = boardRepository.findByTitleContaining(searchTitle, pageable);
        List<BoardDto> boardDtoList = new ArrayList<>();

        for (Board board : boards) {
            BoardDto dto = BoardDto.builder()
                    .board(board)
                    .build();
            boardDtoList.add(dto);
        }
        return new PageImpl<>(boardDtoList, pageable, boards.getTotalElements());
    }

    public BoardDto saveBoard(BoardDto boardDto){
        Board board = Board.builder()
                .writer(boardDto.getWriter())
                .title(boardDto.getTitle())
                .content(boardDto.getContent())
                .build();
        boardRepository.save(board);

        return new BoardDto(board);
    }

    public BoardDto getBoard(Long id){
        Board board = ServiceUtil.findByIdOrThrow(boardRepository, id, DomainException.notFindRow(id));
        BoardDto boardDto = BoardDto.builder()
                .board(board)
                .build();
        return boardDto;
    }

    public BoardDto updateBoard(Long id, BoardDto dto){
        Board board = ServiceUtil.findByIdOrThrow(boardRepository, id, DomainException.notFindRow(id));
        Board updatedBoard = board.toBuilder()
                .id(dto.getId())
                .writer(dto.getWriter())
                .title(dto.getTitle())
                .content(dto.getContent())
                .build();
        Board savedBoard = boardRepository.save(updatedBoard);
        return new BoardDto(savedBoard);
    }


    public void deleteBoard(Long id){
        Board board = ServiceUtil.findByIdOrThrow(boardRepository, id, DomainException.notFindRow(id));
        board.softDelete();
        boardRepository.save(board);
    }


    public List<Reply> getReplyList(Long boardId){
        Board board = ServiceUtil.findByIdOrThrow(boardRepository, boardId, DomainException.notFindRow(boardId));
        List<Reply> replyList = board.getReplyList();
        return replyList;
    }
}

