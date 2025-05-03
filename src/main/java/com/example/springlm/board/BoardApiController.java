package com.example.springlm.board;

import com.example.springlm.common.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/boards")
@RequiredArgsConstructor
public class BoardApiController {
    private final BoardService boardService;

    // 실제 사용되지 않음 - 테스트용
    
    @GetMapping
    public ResponseEntity<Page<BoardDto>> list(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestParam(defaultValue = "") String searchTitle
    ) {
        Page<BoardDto> boardDtoPage = boardService.boardSearchList(searchTitle, pageable);
        if(boardDtoPage.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(boardDtoPage);
    }
    

    @PostMapping
    public ResponseEntity<BoardDto> createBoard(@RequestBody BoardDto dto){
        BoardDto boardDto = boardService.saveBoard(dto);
        return ResponseEntity.ok(boardDto);
    }

    // 실제 사용되지 않음 - 테스트용
    @GetMapping("/{boardId}")
    public ResponseEntity<BoardDto> readBoard(@PathVariable("boardId") Long id){
        try{
            BoardDto boardDto = boardService.getBoard(id);
            return ResponseEntity.ok(boardDto);
        } catch (DomainException e) {
            if (e.getMessage().contains("존재하지 않습니다")) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
        
    }
    

    // 실제 사용되지 않음 - 테스트용
    
    @PutMapping("/{boardId}")
    public ResponseEntity<BoardDto> updateBoard(@PathVariable("boardId") Long id, @RequestBody BoardDto dto){
        BoardDto boardDto = boardService.updateBoard(id, dto);
        return ResponseEntity.ok(boardDto);
    }
    


    // 실제 사용되지 않음 - 테스트용
    
    @DeleteMapping("/{boardId}")
    public ResponseEntity<Void> deleteBoard(@PathVariable("boardId") Long id){
        boardService.deleteBoard(id);
        return ResponseEntity.ok().build();
    }

}
