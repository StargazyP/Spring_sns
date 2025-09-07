package kr.co.inhatc.inhatc.dto;

import java.time.LocalDateTime;

import kr.co.inhatc.inhatc.entity.PostEntity;
import lombok.Getter;

@Getter
public class PostResponseDTO {

    private Long id; // PK

    private String content; // 내용
    private String writer; // 작성자
    private int hits; // 조회 수
    private char deleteYn; // 삭제 여부
    private LocalDateTime createdDate; // 생성일
    private LocalDateTime modifiedDate; // 수정일
    private String imgsource;
    private int love;

    public PostResponseDTO(PostEntity entity) {
        this.id = entity.getId();
        this.imgsource = entity.getImgsource();
        this.love = entity.getLove();
        this.content = entity.getContent();
        this.writer = entity.getWriter();
        this.hits = entity.getHits();
        this.deleteYn = entity.getDeleteYn();
        this.createdDate = entity.getCreatedDate();
        this.modifiedDate = entity.getModifiedDate();
    }

}
