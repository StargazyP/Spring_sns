package kr.co.inhatc.inhatc;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import kr.co.inhatc.inhatc.dto.PostRequestDTO;
import kr.co.inhatc.inhatc.dto.PostResponseDTO;
import kr.co.inhatc.inhatc.service.PostService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 특정 사용자 게시글 조회
     */
    @GetMapping("/user/{writer}")
    public ResponseEntity<List<PostResponseDTO>> getPostsByWriter(@PathVariable String writer) {
        return ResponseEntity.ok(postService.findByUserID(writer));
    }

    /**
     * 게시글 전체 조회
     */
    @GetMapping
    public ResponseEntity<List<PostResponseDTO>> getAllPosts() {
        return ResponseEntity.ok(postService.findAll());
    }

    /**
     * 삭제 여부 기준으로 게시글 조회
     */
    @GetMapping("/deleted/{deleteYn}")
    public ResponseEntity<List<PostResponseDTO>> getPostsByDeleteYn(@PathVariable char deleteYn) {
        return ResponseEntity.ok(postService.findAllByDeleteYn(deleteYn));
    }

    /**
     * 게시글 상세 조회 (조회수 증가 포함)
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDTO> getPostDetail(@PathVariable Long id) {
        return ResponseEntity.ok(postService.findById(id));
    }

    /**
     * 게시글 생성
     */
    @PostMapping
    public ResponseEntity<String> createPost(@RequestBody PostRequestDTO postRequestDTO) {
        postService.save(postRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("게시글이 등록되었습니다.");
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
     * 게시글 이미지 업로드
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("email") String email) {

        try {
            String filePath = postService.imgupload(file, email);
            return ResponseEntity.ok("업로드 성공: " + filePath);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("이미지 업로드 실패: " + e.getMessage());
        }
    }
}
