package kr.co.inhatc.inhatc;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.co.inhatc.inhatc.dto.PostLikeResponseDTO;
import kr.co.inhatc.inhatc.service.LikeService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    /**
     * 특정 사용자가 작성한 게시물에 대해 좋아요 누른 사용자 조회
     */
    @GetMapping("/check/{userId}")
    public ResponseEntity<List<PostLikeResponseDTO>> checkPostLikes(@PathVariable String userId) {
        List<PostLikeResponseDTO> result = likeService.postCheck(userId);
        return ResponseEntity.ok(result);
    }
}
