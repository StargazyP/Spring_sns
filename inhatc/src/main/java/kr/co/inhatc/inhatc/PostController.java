package kr.co.inhatc.inhatc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import kr.co.inhatc.inhatc.dto.PostResponseDTO;
import kr.co.inhatc.inhatc.service.PostService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    /**
     * 특정 사용자 게시글 조회 (memberId 기준)
     */
    @GetMapping("/user/id/{memberId}")
    public ResponseEntity<List<PostResponseDTO>> getPostsByMemberId(@PathVariable Long memberId) {
        return ResponseEntity.ok(postService.findByMemberId(memberId));
    }

    /**
     * 특정 사용자 게시글 조회 (memberName 기준)
     */
    @GetMapping("/user/name/{memberName}")
    public ResponseEntity<List<PostResponseDTO>> getPostsByMemberName(@PathVariable String memberName) {
        return ResponseEntity.ok(postService.findByMemberName(memberName));
    }

    // 전체 게시글 조회 (삭제되지 않은 것만)
    @GetMapping
    public ResponseEntity<List<PostResponseDTO>> getAllPosts() {
        List<PostResponseDTO> posts = postService.findAll();
        System.out.println("DTO 수: " + posts.size());
        return ResponseEntity.ok(posts);
    }

    /**
     * 삭제 여부 기준으로 게시글 조회
     */
    @GetMapping("/deleted/{deleteYn}")
    public ResponseEntity<List<PostResponseDTO>> getPostsByDeleteYn(@PathVariable char deleteYn) {
        return ResponseEntity.ok(postService.findAllByDeleteYn(deleteYn));
    }

    // 개별 게시글 조회
    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDTO> getPostById(@PathVariable Long id) {
        PostResponseDTO post = postService.findById(id);
        return ResponseEntity.ok(post);
    }

    /**
     * 게시글 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePost(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.ok("게시글이 삭제되었습니다.");
    }

    /**
     * 좋아요 토글
     */
    @PostMapping("/{postId}/likes")
    public ResponseEntity<Map<String, Object>> toggleLove(
            @PathVariable Long postId,
            @RequestParam String email) {

        boolean liked = postService.toggleLove(postId, email);
        Map<String, Object> response = new HashMap<>();
        response.put("liked", liked);
        response.put("postId", postId);
        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 업로드
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("content") String content,
            HttpSession session) {

        String email = (String) session.getAttribute("loginEmail");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        try {
            // 이미지 업로드 후 URL 반환
            String fileUrl = postService.imgupload(file, email);

            // 게시글 저장 (DB에는 URL 저장)
            postService.savePost(email, content, fileUrl);

            return ResponseEntity.ok("게시물 업로드 성공");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("게시물 업로드 실패: " + e.getMessage());
        }
    }

}
