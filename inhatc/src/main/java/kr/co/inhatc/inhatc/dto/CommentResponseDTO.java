package kr.co.inhatc.inhatc.dto;

import java.time.LocalDateTime;

import kr.co.inhatc.inhatc.entity.CommentEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentResponseDTO {
    private Long id; // 댓글 ID
    private Long post; // 댓글이 속한 게시글 ID
    private String comment; // 내용
    private String writer; // 작성자
    private LocalDateTime createdDate; // 생성일
    // private LocalDateTime modifiedDate; // 수정일

    @Builder
    public CommentResponseDTO(Long id, Long post, String comment, String writer, LocalDateTime createdDate) {
        this.id = id;
        this.post = post;
        this.comment = comment;
        this.writer = writer;
        this.createdDate = createdDate;
    }

    public CommentResponseDTO(CommentEntity commentEntity) {
        this.id = commentEntity.getId();
        this.post = commentEntity.getPost().getId();
        this.comment = commentEntity.getComment();
        this.writer = commentEntity.getWriter().getMemberEmail();
        this.createdDate = commentEntity.getCreateDate();
    }
    
}
