package kr.co.inhatc.inhatc.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final LikeRepository likeRepository;
    private final NotificationService notificationService;
    
    @Value("${app.upload.posts-dir}")
    private String postsUploadDir;

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
     * ✅ 회원 ID 기준 조회 (N+1 문제 해결)
     */
    public List<PostResponseDTO> findByMemberId(Long memberId) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        
        // JOIN FETCH로 Comments를 한 번에 조회
        List<PostEntity> posts = postRepository.findByMemberEmailWithComments(member.getMemberEmail());

        // 동일한 회원의 게시글이므로 Member는 한 번만 조회
        return posts.stream()
                .map(post -> PostResponseDTO.fromEntity(post, member))
                .collect(Collectors.toList());
    }

    /**
     * ✅ 회원 이름 기준 조회 (N+1 문제 해결)
     */
    public List<PostResponseDTO> findByMemberName(String memberName) {
        List<MemberEntity> members = memberRepository.findByMemberName(memberName);
        
        if (members.isEmpty()) {
            return new ArrayList<>();
        }

        // 모든 회원의 이메일 수집
        List<String> memberEmails = members.stream()
                .map(MemberEntity::getMemberEmail)
                .collect(Collectors.toList());

        // JOIN FETCH로 Comments를 한 번에 조회
        List<PostEntity> posts = postRepository.findByMemberEmailsWithComments(memberEmails);

        // Map으로 변환하여 O(1) 조회 가능하도록 최적화
        Map<String, MemberEntity> memberMap = members.stream()
                .collect(Collectors.toMap(MemberEntity::getMemberEmail, member -> member));

        return posts.stream()
                .map(post -> PostResponseDTO.fromEntity(post, memberMap.get(post.getMemberEmail())))
                .collect(Collectors.toList());
    }

    /**
     * ✅ 회원 이메일 기준으로 게시글 조회 (삭제되지 않은 것만, N+1 문제 해결)
     */
    public List<PostResponseDTO> findByMemberEmail(String memberEmail) {
        // JOIN FETCH로 Comments를 한 번에 조회
        List<PostEntity> posts = postRepository.findByMemberEmailWithComments(memberEmail);
        MemberEntity member = memberRepository.findByMemberEmail(memberEmail).orElse(null);

        return posts.stream()
                .filter(post -> post.getDeleteYn() == 'N') // 삭제되지 않은 게시글만
                .map(post -> PostResponseDTO.fromEntity(post, member))
                .collect(Collectors.toList());
    }

    /**
     * ✅ 삭제 여부 기준 조회 (N+1 문제 해결)
     */
    public List<PostResponseDTO> findAllByDeleteYn(char deleteYn) {
        // JOIN FETCH로 Comments를 한 번에 조회
        List<PostEntity> posts = postRepository.findAllWithCommentsByDeleteYn(deleteYn);

        // 모든 게시글의 memberEmail 수집
        List<String> memberEmails = posts.stream()
                .map(PostEntity::getMemberEmail)
                .distinct()
                .collect(Collectors.toList());

        // 한 번의 쿼리로 모든 Member 조회 (N+1 방지)
        List<MemberEntity> members = memberRepository.findByMemberEmailIn(memberEmails);
        
        // Map으로 변환하여 O(1) 조회 가능하도록 최적화
        Map<String, MemberEntity> memberMap = members.stream()
                .collect(Collectors.toMap(MemberEntity::getMemberEmail, member -> member));

        return posts.stream()
                .map(post -> PostResponseDTO.fromEntity(post, memberMap.get(post.getMemberEmail())))
                .collect(Collectors.toList());
    }

    /**
     * ✅ 삭제되지 않은 게시글 전체 조회 (N+1 문제 해결)
     */
    public List<PostResponseDTO> findAll() {
        // JOIN FETCH로 Comments를 한 번에 조회
        List<PostEntity> posts = postRepository.findAllWithCommentsByDeleteYn('N');

        // 모든 게시글의 memberEmail 수집
        List<String> memberEmails = posts.stream()
                .map(PostEntity::getMemberEmail)
                .distinct()
                .collect(Collectors.toList());

        // 한 번의 쿼리로 모든 Member 조회 (N+1 방지)
        List<MemberEntity> members = memberRepository.findByMemberEmailIn(memberEmails);
        
        // Map으로 변환하여 O(1) 조회 가능하도록 최적화
        Map<String, MemberEntity> memberMap = members.stream()
                .collect(Collectors.toMap(MemberEntity::getMemberEmail, member -> member));

        return posts.stream()
                .map(post -> PostResponseDTO.fromEntity(post, memberMap.get(post.getMemberEmail())))
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
                log.warn("알림 생성 실패: postId={}, email={}", postId, email, e);
            }
        }
        return existingLike.isEmpty();
    }

    /**
     * 게시글 이미지 업로드
     * 공통 유틸리티 사용으로 중복 코드 제거
     */
    public String imgupload(MultipartFile file, String email) throws IOException {
        return kr.co.inhatc.inhatc.util.FileUploadService.uploadPostImage(file, email, postsUploadDir);
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
                .createdDate(LocalDateTime.now()) // 명시적으로 생성 날짜 설정
                .build();

        postRepository.save(post);
    }

    /**
     * ✅ 페이징 지원: 삭제되지 않은 게시글 조회 (페이징)
     */
    public Page<PostResponseDTO> findAll(Pageable pageable) {
        // @EntityGraph로 Comments를 한 번에 조회
        Page<PostEntity> postPage = postRepository.findByDeleteYnOrderByCreatedDateDesc('N', pageable);
        
        // 모든 게시글의 memberEmail 수집
        List<String> memberEmails = postPage.getContent().stream()
                .map(PostEntity::getMemberEmail)
                .distinct()
                .collect(Collectors.toList());

        // 한 번의 쿼리로 모든 Member 조회 (N+1 방지)
        List<MemberEntity> members = memberRepository.findByMemberEmailIn(memberEmails);
        
        // Map으로 변환하여 O(1) 조회 가능하도록 최적화
        Map<String, MemberEntity> memberMap = members.stream()
                .collect(Collectors.toMap(MemberEntity::getMemberEmail, member -> member));

        return postPage.map(post -> PostResponseDTO.fromEntity(post, memberMap.get(post.getMemberEmail())));
    }

    /**
     * ✅ 페이징 지원: 특정 이메일의 게시글 조회 (페이징)
     */
    public Page<PostResponseDTO> findByMemberEmail(String memberEmail, Pageable pageable) {
        // @EntityGraph로 Comments를 한 번에 조회
        Page<PostEntity> postPage = postRepository.findByMemberEmailOrderByIdDesc(memberEmail, pageable);
        MemberEntity member = memberRepository.findByMemberEmail(memberEmail).orElse(null);

        return postPage.map(post -> PostResponseDTO.fromEntity(post, member));
    }

}
