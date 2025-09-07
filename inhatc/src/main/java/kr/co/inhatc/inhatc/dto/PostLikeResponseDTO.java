package kr.co.inhatc.inhatc.dto;

import lombok.Getter;

@Getter
public class PostLikeResponseDTO {

    private Long postId;
    private String message;

    public PostLikeResponseDTO(Long postId, String message) {
        this.postId = postId;
        this.message = message;
    }
}
