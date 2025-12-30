package kr.co.inhatc.inhatc.dto;

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
public class FollowStatsDTO {

    private Long followerCount; // 팔로워 수
    private Long followingCount; // 팔로잉 수
    private boolean isFollowing; // 현재 로그인한 사용자가 이 사용자를 팔로우하는지 여부
}



