package kr.co.inhatc.inhatc.dto;

import java.time.LocalDateTime;

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

    private String comment;

    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();

    // @Builder.Default
    // private LocalDateTime modifiedDate = LocalDateTime.now();

    private String user;

    private Long article;

    public CommentRequestDTO toEntity() {
        return CommentRequestDTO.builder()
                .comment(comment)
                .createdDate(createdDate)
                // .modifiedDate(modifiedDate)
                .user(user)
                .article(article)
                .build();
    }
}
