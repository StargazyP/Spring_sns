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
    private String writer; // 작성자 이메일
    private String writerName;
    private String writerProfile; // 작성자 프로필 이미지 경로
    private LocalDateTime createdDate; // 생성일

    @Builder
    public CommentResponseDTO(Long id, Long post, String comment, String writer, String writerProfile,
    String writerName,
                              LocalDateTime createdDate) {
        this.id = id;
        this.post = post;
        this.comment = comment;
        this.writer = writer;
        this.writerName = writerName;
        this.writerProfile = writerProfile;
        this.createdDate = createdDate;
    }

    public CommentResponseDTO(CommentEntity commentEntity) {
        this.id = commentEntity.getId();
        this.post = commentEntity.getPost().getId();
        this.comment = commentEntity.getComment();

        if (commentEntity.getWriter() != null) {
            this.writer = commentEntity.getWriter().getMemberEmail();
            this.writerName = commentEntity.getWriter().getMemberName();

            // 프로필 경로 설정: WebConfig의 /static/** 매핑 활용
            // 저장 경로: C:\Users\jdajs\spring test\inhatc\src\main\resources\static\{email}\profile.png
            // 접근 경로: /static/{email}/profile.png
            String email = commentEntity.getWriter().getMemberEmail();
            if (email != null && !email.isEmpty()) {
                // WebConfig 매핑을 통해 /static/{email}/profile.png로 직접 접근
                this.writerProfile = "/static/" + email + "/profile.png";
            } else {
                this.writerProfile = "/images/default-profile.png";
            }
        } else {
            // 작성자가 없는 경우
            this.writer = "unknown";
            this.writerProfile = "/images/default-profile.png";
        }

        this.createdDate = commentEntity.getCreateDate();
    }
}
