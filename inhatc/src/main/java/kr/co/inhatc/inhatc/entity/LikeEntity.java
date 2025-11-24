package kr.co.inhatc.inhatc.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "likes_entity")
public class LikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id") // FK: LikeEntity.post_id → PostEntity.id
    private PostEntity post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id") // FK: LikeEntity.member_id → MemberEntity.id
    private MemberEntity user;

    @Column(name = "liked_date", nullable = false, updatable = false)
    private LocalDateTime likedDate;

    public LikeEntity(PostEntity post, MemberEntity user) {
        this.post = post;
        this.user = user;
        this.likedDate = LocalDateTime.now();
    }
}