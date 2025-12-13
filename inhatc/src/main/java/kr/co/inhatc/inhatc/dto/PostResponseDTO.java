package kr.co.inhatc.inhatc.dto;

import java.nio.file.Paths;
import java.time.LocalDateTime;

import kr.co.inhatc.inhatc.entity.MemberEntity;
import kr.co.inhatc.inhatc.entity.PostEntity;
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
public class PostResponseDTO {

    private Long id;
    private String content;
    private int hits;
    private int love;
    private String imgsource;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    private String writerName;
    private String writer; // HTML 템플릿 호환성을 위한 별칭
    private String writerEmail;
    private String profilePicturePath;

    public PostResponseDTO(PostEntity post, MemberEntity member) {
        this.id = post.getId();
        this.content = post.getContent();
        this.hits = post.getHits();
        this.love = post.getLove();
        this.createdDate = post.getCreatedDate();
        this.modifiedDate = post.getModifiedDate();

        // 게시글 이미지 (/posts/**로 접근)
        if (post.getImgsource() != null && !post.getImgsource().isEmpty()) {
            String imgsource = post.getImgsource();
            // 이미 /posts/로 시작하는 경우 그대로 사용
            if (imgsource.startsWith("/posts/")) {
                this.imgsource = imgsource;
            } else {
                // DB에 저장된 경로가 상대 경로인 경우 처리
                try {
                    // 파일명 추출 (경로에서 마지막 부분)
                    String filename = Paths.get(imgsource).getFileName().toString();
                    String email = post.getMemberEmail();
                    this.imgsource = "/posts/" + email + "/" + filename; // 게시물 이미지 URL
                } catch (Exception e) {
                    // 파싱 실패 시 원본 경로 사용 또는 기본값
                    this.imgsource = imgsource.startsWith("/") ? imgsource : "/posts/" + imgsource;
                }
            }
        }

        // 작성자 프로필 이미지 (/static/{email}/profile.png로 접근 - WebConfig 매핑 활용)
        if (member != null) {
            this.writerName = member.getMemberName();
            this.writer = member.getMemberName(); // HTML 템플릿 호환성
            this.writerEmail = member.getMemberEmail();
            
            // 프로필 이미지 경로 설정: WebConfig의 /static/** 매핑 활용
            // 저장 경로: C:\Users\jdajs\spring test\inhatc\src\main\resources\static\{email}\profile.png
            // 접근 경로: /static/{email}/profile.png
            String email = member.getMemberEmail();
            if (email != null && !email.isEmpty()) {
                // WebConfig 매핑을 통해 /static/{email}/profile.png로 직접 접근
                this.profilePicturePath = "/static/" + email + "/profile.png";
            } else {
                this.profilePicturePath = "/images/default-profile.png";
            }
        } else {
            this.writerName = "익명";
            this.writer = "익명"; // HTML 템플릿 호환성
            this.writerEmail = "unknown";
            this.profilePicturePath = "/images/default-profile.png";
        }
    }

    public static PostResponseDTO fromEntity(PostEntity post, MemberEntity member) {
        return new PostResponseDTO(post, member);
    }

}
