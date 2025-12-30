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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "follow_entity", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "following_id"}))
public class FollowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false) // 팔로우하는 사람
    private MemberEntity follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false) // 팔로우 받는 사람
    private MemberEntity following;

    @Column(name = "follow_date", nullable = false, updatable = false)
    private LocalDateTime followDate;

    public FollowEntity(MemberEntity follower, MemberEntity following) {
        this.follower = follower;
        this.following = following;
        this.followDate = LocalDateTime.now();
    }
}



