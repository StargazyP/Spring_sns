package kr.co.inhatc.inhatc.service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import kr.co.inhatc.inhatc.dto.PostResponseDTO;
import kr.co.inhatc.inhatc.entity.LikeEntity;
import kr.co.inhatc.inhatc.entity.MemberEntity;
import kr.co.inhatc.inhatc.entity.PostEntity;
import kr.co.inhatc.inhatc.exception.CustomException;
import kr.co.inhatc.inhatc.exception.ErrorCode;
import kr.co.inhatc.inhatc.repository.LikeRepository;
import kr.co.inhatc.inhatc.repository.MemberRepository;
import kr.co.inhatc.inhatc.repository.PostRepository;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final LikeRepository likeRepository;
    private final NotificationService notificationService;
    private final String baseUploadDir = "C:/Users/jdajs/spring test/inhatc/src/main/resources/";

    // @Lazy를 생성자 파라미터에 적용하여 순환 참조 방지
    public PostService(PostRepository postRepository, 
                       MemberRepository memberRepository, 
                       LikeRepository likeRepository,
                       @Lazy NotificationService notificationService) {
        this.postRepository = postRepository;
        this.memberRepository = memberRepository;
        this.likeRepository = likeRepository;
        this.notificationService = notificationService;
    }

    /**
     * ✅ 회원 ID 기준 조회
     */
    public List<PostResponseDTO> findByMemberId(Long memberId) {
        List<PostEntity> posts = postRepository.findByMemberEmailOrderByIdDesc(
                memberRepository.findById(memberId)
                        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND))
                        .getMemberEmail());

        return posts.stream()
                .map(post -> {
                    MemberEntity member = memberRepository.findByMemberEmail(post.getMemberEmail())
                            .orElse(null);
                    return PostResponseDTO.fromEntity(post, member);
                })
                .collect(Collectors.toList());
    }

    /**
     * ✅ 회원 이름 기준 조회
     */
    public List<PostResponseDTO> findByMemberName(String memberName) {
        List<MemberEntity> members = memberRepository.findByMemberName(memberName);
        List<PostResponseDTO> result = new ArrayList<>();

        for (MemberEntity member : members) {
            List<PostEntity> posts = postRepository.findByMemberEmailOrderByIdDesc(member.getMemberEmail());
            for (PostEntity post : posts) {
                result.add(PostResponseDTO.fromEntity(post, member));
            }
        }

        return result;
    }

    /**
     * ✅ 회원 이메일 기준으로 게시글 조회 (삭제되지 않은 것만)
     */
    public List<PostResponseDTO> findByMemberEmail(String memberEmail) {
        List<PostEntity> posts = postRepository.findByMemberEmailOrderByIdDesc(memberEmail);
        MemberEntity member = memberRepository.findByMemberEmail(memberEmail).orElse(null);

        return posts.stream()
                .filter(post -> post.getDeleteYn() == 'N') // 삭제되지 않은 게시글만
                .map(post -> PostResponseDTO.fromEntity(post, member))
                .collect(Collectors.toList());
    }

    /**
     * ✅ 삭제 여부 기준 조회
     */
    public List<PostResponseDTO> findAllByDeleteYn(char deleteYn) {
        List<PostEntity> posts = postRepository.findByDeleteYnOrderByCreatedDateDesc(deleteYn);

        return posts.stream()
                .map(post -> {
                    MemberEntity member = memberRepository.findByMemberEmail(post.getMemberEmail())
                            .orElse(null);
                    return PostResponseDTO.fromEntity(post, member);
                })
                .collect(Collectors.toList());
    }

    /**
     * ✅ 삭제되지 않은 게시글 전체 조회
     */
    public List<PostResponseDTO> findAll() {
        List<PostEntity> posts = postRepository.findByDeleteYnOrderByCreatedDateDesc('N');

        return posts.stream()
                .map(post -> {
                    MemberEntity member = memberRepository.findByMemberEmail(post.getMemberEmail())
                            .orElse(null);
                    return PostResponseDTO.fromEntity(post, member);
                })
                .collect(Collectors.toList());
    }

    /**
     * ✅ 게시글 단건 조회 + 조회수 증가
     */
    @Transactional
    public PostResponseDTO findById(Long id) {
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.POSTS_NOT_FOUND));

        post.increaseHits();

        MemberEntity member = memberRepository.findByMemberEmail(post.getMemberEmail())
                .orElse(null);

        return PostResponseDTO.fromEntity(post, member);
    }

    /**
     * 좋아요 토글
     */
    @Transactional
    public boolean toggleLove(Long postId, String email) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POSTS_NOT_FOUND));
        MemberEntity user = memberRepository.findByMemberEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Optional<LikeEntity> existingLike = likeRepository.findByPostAndUser(post, user);
        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            post.decreaseLove(); // <- 여기 메서드 이름 변경
        } else {
            LikeEntity newLike = new LikeEntity(post, user);
            likeRepository.save(newLike);
            post.increaseLove(); // <- 여기 메서드 이름 변경
            
            // 좋아요 알림 생성
            try {
                notificationService.createLikeNotification(postId, email);
            } catch (Exception e) {
                // 알림 생성 실패해도 좋아요는 정상 처리
                System.err.println("알림 생성 실패: " + e.getMessage());
            }
        }
        return existingLike.isEmpty();
    }

    /**
     * 게시글 이미지 업로드
     */
    public String imgupload(MultipartFile file, String email) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 외부 절대 경로 (게시물 이미지 저장 경로)
        String baseDir = "C:/Users/jdajs/spring test/inhatc/src/main/java/kr/co/inhatc/inhatc/";

        // 사용자 폴더 생성
        File userDir = new File(baseDir, email);
        if (!userDir.exists()) {
            if (!userDir.mkdirs()) {
                throw new IOException("사용자 디렉토리 생성 실패: " + userDir.getAbsolutePath());
            }
        }

        // 파일 확장자 추출
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.lastIndexOf('.') != -1) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
        }

        // 새 파일명 생성 (날짜 + 밀리초)
        String newFilename = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        if (!extension.isEmpty()) {
            newFilename += "." + extension;
        }

        // 파일 저장
        File dest = new File(userDir, newFilename);
        file.transferTo(dest);

        // DB에 저장할 URL 경로
        String urlPath = "/posts/" + email + "/" + newFilename;
        System.out.println("파일 저장 및 DB 기록 URL: " + urlPath);

        return urlPath;
    }

    @Transactional
    public void delete(Long postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POSTS_NOT_FOUND));

        // 실제 DB에서 삭제
        // postRepository.delete(post);

        // 또는 삭제 여부만 처리하고 싶으면 아래처럼 변경 가능
        post.setDeleteYn('Y');
        postRepository.save(post);
    }

    /**
     * 게시글 저장
     */
    @Transactional
    public void savePost(String email, String content, String filePath) {
        MemberEntity member = memberRepository.findByMemberEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        PostEntity post = PostEntity.builder()
                .memberEmail(member.getMemberEmail())
                .content(content)
                .imgsource(filePath) // DB에는 상대 경로 저장
                .hits(0)
                .love(0)
                .deleteYn('N')
                .build();

        postRepository.save(post);
    }

}
