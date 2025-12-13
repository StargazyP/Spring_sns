package kr.co.inhatc.inhatc.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRequestDTO {
    private Long id;

    @NotBlank(message = "댓글 내용은 필수입니다.")
    @Size(max = 1000, message = "댓글은 1000자 이하여야 합니다.")
    private String comment;

    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();

    @NotBlank(message = "사용자 이메일은 필수입니다.")
    private String user;

    @NotNull(message = "게시글 ID는 필수입니다.")
    private Long article;

    /** 부모 댓글 ID (대댓글인 경우) */
    private Long parentCommentId;

    public CommentRequestDTO toEntity() {
        return CommentRequestDTO.builder()
                .comment(comment)
                .createdDate(createdDate)
                .user(user)
                .article(article)
                .parentCommentId(parentCommentId)
                .build();
    }
}
