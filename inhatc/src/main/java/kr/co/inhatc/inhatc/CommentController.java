package kr.co.inhatc.inhatc;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import kr.co.inhatc.inhatc.dto.CommentRequestDTO;
import kr.co.inhatc.inhatc.dto.CommentResponseDTO;
import kr.co.inhatc.inhatc.service.CommentService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 게시글별 댓글 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<CommentResponseDTO>> getComments(@PathVariable Long postId) {
        List<CommentResponseDTO> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    /**
     * 댓글 작성
     */
    @PostMapping
    public ResponseEntity<CommentResponseDTO> addComment(
            @PathVariable Long postId,
            @RequestBody CommentRequestDTO commentRequestDTO,
            HttpSession session) {

        // 로그인된 사용자 이메일을 세션에서 가져옴
        String loginEmail = (String) session.getAttribute("loginEmail");

        // DTO에 세팅
        commentRequestDTO.setUser(loginEmail);
        commentRequestDTO.setArticle(postId);

        CommentResponseDTO savedComment = commentService.addComment(commentRequestDTO);
        return ResponseEntity.ok(savedComment);
    }

}
