package kr.co.inhatc.inhatc.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table; 
import kr.co.inhatc.inhatc.dto.MemberDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "member_entity")
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_email", nullable = false, unique = true)
    private String memberEmail;

    @Column(name = "member_password", nullable = false)
    private String memberPassword;

    @Column(name = "member_name", nullable = false)
    private String memberName;

    @Column(name = "profile_picture_path")
    private String profilePicturePath;

    /** 게시글: 단방향, PostEntity에 memberEmail 기준으로 연결 */
    @OneToMany
    @JoinColumn(name = "member_email", referencedColumnName = "member_email", insertable = false, updatable = false)
    private List<PostEntity> posts = new ArrayList<>();

    /** 댓글: 단방향, CommentEntity에 writer 기준으로 연결 */
    @OneToMany
    @JoinColumn(name = "writer", referencedColumnName = "member_email", insertable = false, updatable = false)
    private List<CommentEntity> comments = new ArrayList<>();

    @ManyToMany(mappedBy = "lovedBy")
    private Set<PostEntity> likedPosts = new HashSet<>();

    // DTO 변환 메서드
    public static MemberDTO toDTO(MemberEntity member) {
        // profilePicturePath 처리: 전체 경로인 경우 그대로 사용, URL 형식인 경우 그대로 사용
        String profilePath = member.getProfilePicturePath();
        if (profilePath != null && !profilePath.isEmpty()) {
            // 전체 경로인 경우 C:\ 또는 /로 시작하는 절대 경로
            if (profilePath.contains(":\\") || profilePath.startsWith("C:") || profilePath.startsWith("/")) {
                // 전체 경로는 그대로 저장 (Controller에서 처리)
                // 변경 없음
            } else {
                // 상대 경로인 경우 /images/ 추가
                profilePath = "/images/" + profilePath;
            }
        }
        // null이거나 빈 문자열이면 null로 설정 (기본 이미지 사용)
        
        return MemberDTO.builder()
                .id(member.getId())
                .memberEmail(member.getMemberEmail())
                .memberName(member.getMemberName())
                .memberPassword(member.getMemberPassword())
                .profilePicturePath(profilePath)
                .build();
    }

    public static MemberEntity fromDTO(MemberDTO dto) {
        return MemberEntity.builder()
                .id(dto.getId())
                .memberEmail(dto.getMemberEmail())
                .memberName(dto.getMemberName())
                .memberPassword(dto.getMemberPassword())
                .profilePicturePath(dto.getProfilePicturePath())
                .build();
    }
}
