package kr.co.inhatc.inhatc.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
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

// CommentEntity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "comments_entity")
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String comment;

    /**  member_email 기준으로 연결 */
    @Column(name = "member_email", nullable = false)
    private String memberEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_email", referencedColumnName = "member_email", insertable = false, updatable = false)
    private MemberEntity writer;

    /** 게시글 연결 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private PostEntity post;

    /** 부모 댓글 (대댓글인 경우) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private CommentEntity parentComment;

    @Column(name = "create_date", updatable = false)
    @CreatedDate
    private LocalDateTime createDate;

    @Builder
    public CommentEntity(String comment, PostEntity post, MemberEntity writer, CommentEntity parentComment) {
        this.comment = comment;
        this.post = post;
        this.writer = writer;
        this.memberEmail = writer.getMemberEmail();
        this.parentComment = parentComment;
        this.createDate = LocalDateTime.now();
    }

    public void update(String comment) {
        this.comment = comment;
    }
}