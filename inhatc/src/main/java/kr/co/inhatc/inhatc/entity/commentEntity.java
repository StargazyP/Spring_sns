package kr.co.inhatc.inhatc.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "comments_entity")
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(columnDefinition = "TEXT", nullable = false)
    private String comment; // 댓글 내용

    @Column(name = "create_date", updatable = false) // 생성일 수정 불가 설정
    @CreatedDate
    private LocalDateTime createDate; // 생성일

    @ManyToOne
    @JoinColumn(name = "post_id")
    private PostEntity post; // 게시글과의 관계 설정

    @ManyToOne
    @JoinColumn(name = "member_email", referencedColumnName = "memberEmail")
    private MemberEntity writer; // 작성자 정보

    @Builder
    public CommentEntity(String comment, PostEntity post, MemberEntity writer) {
        this.comment = comment;
        this.post = post;
        this.writer = writer;
        this.createDate = LocalDateTime.now();
    }

    public void update(String comment) {
        this.comment = comment;
    }
}
