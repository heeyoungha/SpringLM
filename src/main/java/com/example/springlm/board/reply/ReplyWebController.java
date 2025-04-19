package com.example.springlm.board.reply;

import com.example.springlm.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/board/{boardId}/reply")
@RequiredArgsConstructor
public class ReplyWebController {
    private final ReplyService replyService;

    // 실제 사용되지 않음 - 게시글 상세에서 직접 댓글 목록을 조회함
    /*
    @GetMapping
    public String listReplies(@PathVariable Long boardId, Model model) {
        List<Reply> replies = replyService.getReplyList(boardId);
        model.addAttribute("replies", replies);
        model.addAttribute("boardId", boardId);
        return "board/reply-list";
    }
    */

    // 실제 사용되지 않음 - 템플릿에서 Ajax로 처리
    /*
    @GetMapping("/new")
    public String createReplyForm(@PathVariable Long boardId, Model model) {
        model.addAttribute("replyDto", new ReplyDto());
        model.addAttribute("boardId", boardId);
        return "board/reply-form";
    }
    */

    // 실제 사용되지 않음 - 템플릿에서 Ajax로 API 호출
    /*
    @PostMapping
    public String createReply(@PathVariable Long boardId, @ModelAttribute ReplyDto replyDto, @SessionAttribute(name = "user", required = false) User user) {
        replyService.cerateReply(replyDto, user, boardId);
        return "redirect:/board/" + boardId;
    }
    */

    // 실제 사용되지 않음 - 템플릿에서 Ajax로 처리
    /*
    @GetMapping("/{replyId}/edit")
    public String editReplyForm(@PathVariable Long boardId, @PathVariable Long replyId, Model model) {
        Reply reply = replyService.getReply(replyId);
        model.addAttribute("reply", reply);
        model.addAttribute("replyId", replyId);
        model.addAttribute("boardId", boardId);
        return "board/reply-edit-form";
    }
    */

    // 실제 사용되지 않음 - 템플릿에서 Ajax로 API 호출
    /*
    @PostMapping("/{replyId}/edit")
    public String editReply(@PathVariable Long boardId, @PathVariable Long replyId, @ModelAttribute ReplyDto replyDto) {
        replyService.updateReply(replyId, replyDto);
        return "redirect:/board/" + boardId;
    }
    */

    // 댓글 삭제 처리 - 실제 사용됨 (Ajax에서 호출)
    @DeleteMapping("/{replyId}")
    @ResponseBody
    public String deleteReply(@PathVariable Long boardId, @PathVariable Long replyId) {
        replyService.deleteReply(replyId);
        return "ok";
    }
} 