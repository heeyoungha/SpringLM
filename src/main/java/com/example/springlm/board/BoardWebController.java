package com.example.springlm.board;

import com.example.springlm.board.reply.Reply;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class BoardWebController {
    private final BoardService boardService;
    private static final Logger logger = LoggerFactory.getLogger(BoardWebController.class);

    @GetMapping(value = {"/boardList"})
    public String list(Model model,
                       @PageableDefault(page = 0, size = 5, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable,
                       @RequestParam(required = false) String searchKeyword
    ) {
        try {
            logger.info("Entering list method with searchTitle: {}, pageable: {}", searchKeyword, pageable);

            Page<BoardDto> boardDtoPage = boardService.boardSearchList(searchKeyword, pageable);

            int nowPage = boardDtoPage.getPageable().getPageNumber() + 1; //pageable에서 넘어온 현재페이지를 가지고올수있다 * 0부터시작하니까 +1
            int startPage = Math.max(nowPage - 4, 1); //매개변수로 들어온 두 값을 비교해서 큰값을 반환
            int endPage = Math.min(nowPage + 5, boardDtoPage.getTotalPages());

            model.addAttribute("boardList" , boardDtoPage);
            model.addAttribute("nowPage", nowPage);
            model.addAttribute("startPage", startPage);
            model.addAttribute("endPage", endPage);
            model.addAttribute("searchKeyword", searchKeyword);

            logger.info("Returning view: board/get-boardlist");
            return "board/get-boardlist";
        }catch (Exception e) {
            logger.error("Error occurred in list method", e);
            throw e; // 예외를 다시 던져서 Spring이 처리하도록 합니다.
        }

    }

    @GetMapping("/board")
    public String getCreateBoard(){

        logger.debug("getCreateBoard()");
        return "board/create-board.html";
    }

    // 실제 사용되지 않음 - 작성 폼에서 Ajax로 API 직접 호출 (/api/board POST)
    /*
    @PostMapping("/board")
    public BoardDto createBoard(@RequestBody BoardDto dto){

        logger.debug("Received request to create board with title: {}, content: {}", dto.getTitle(), dto.getContent());

        BoardDto savedBoard = boardService.saveBoard(dto);

        logger.debug("Board created with ID: {}", savedBoard.getId());

        return savedBoard;
    }
    */

    @GetMapping("/board/{boardId}")
    public String readBoard(@PathVariable("boardId") Long id, Model model){
        BoardDto boardDto = boardService.getBoard(id);
        List<Reply> replyList = boardService.getReplyList(id);
        model.addAttribute("boardDto", boardDto);
        model.addAttribute("replyList", replyList);

        return "board/board-detail";
    }

    @GetMapping("/board/edit/{boardId}")
    public String editBoard(@PathVariable("boardId") Long id, Model model){
        BoardDto boardDto = boardService.getBoard(id);
        model.addAttribute("boardDto", boardDto);
        return "board/edit";
    }

    @PutMapping("/board/edit/{boardId}")
    @ResponseBody
    public ResponseEntity<?> updateBoard(@PathVariable("boardId") Long id, @RequestBody BoardDto dto){
        boardService.updateBoard(id, dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/board/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteBoard(@PathVariable Long id) {
        boardService.deleteBoard(id);
        return ResponseEntity.ok().build();
    }

}
