package kr.co.inhatc.inhatc.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import kr.co.inhatc.inhatc.dto.CommentRequestDTO;
import kr.co.inhatc.inhatc.dto.CommentResponseDTO;
import kr.co.inhatc.inhatc.entity.CommentEntity;
import kr.co.inhatc.inhatc.entity.MemberEntity;
import kr.co.inhatc.inhatc.entity.PostEntity;
import kr.co.inhatc.inhatc.repository.CommentRepository; // 수정된 부분: 대문자로 시작하는 클래스명으로 변경
import kr.co.inhatc.inhatc.repository.MemberRepository;
import kr.co.inhatc.inhatc.repository.PostRepository;

@Service
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    // @Lazy를 생성자 파라미터에 적용하여 순환 참조 방지
    public CommentService(CommentRepository commentRepository,
                          MemberRepository memberRepository,
                          PostRepository postRepository,
                          @Lazy NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.memberRepository = memberRepository;
        this.postRepository = postRepository;
        this.notificationService = notificationService;
    }

    /**
     * ✅ 특정 이메일로 작성된 댓글 조회
     */
    public List<CommentResponseDTO> findByEmail(String email) {
        List<CommentEntity> list = commentRepository.findByWriterMemberEmail(email);
        return list.stream()
                .map(CommentResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * ✅ 게시글별 댓글 전체 조회
     */
    public List<CommentResponseDTO> getCommentsByPostId(Long postId) {
        List<CommentEntity> comments = commentRepository.findByPostIdOrderByCreateDateDesc(postId);
        return comments.stream()
                .map(CommentResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * ✅ 댓글 추가
     */
    public CommentResponseDTO addComment(CommentRequestDTO requestDTO) {
        System.out.println("댓글 작성 요청: user=" + requestDTO.getUser() + ", article=" + requestDTO.getArticle());

        MemberEntity writer = memberRepository.findByMemberEmail(requestDTO.getUser())
                .orElseThrow(() -> new RuntimeException("댓글 작성자를 찾을 수 없습니다."));

        System.out.println("찾은 작성자: " + writer.getMemberEmail());

        PostEntity post = postRepository.findById(requestDTO.getArticle())
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        CommentEntity comment = CommentEntity.builder()
                .comment(requestDTO.getComment())
                .writer(writer)
                .post(post)
                .build();

        CommentEntity saved = commentRepository.save(comment);
        
        // 댓글 알림 생성
        try {
            notificationService.createCommentNotification(post.getId(), requestDTO.getUser());
        } catch (Exception e) {
            // 알림 생성 실패해도 댓글은 정상 처리
            System.err.println("알림 생성 실패: " + e.getMessage());
        }
        
        return new CommentResponseDTO(saved);
    }

    /**
     * ✅ 댓글 수정
     */
    public void updateComment(Long postId, Long commentId, CommentRequestDTO requestDTO) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글이 존재하지 않습니다."));

        // 게시글 불일치 방지
        if (!comment.getPost().getId().equals(postId)) {
            throw new RuntimeException("해당 댓글은 이 게시글에 속하지 않습니다.");
        }

        // 작성자 일치 여부 확인 (보안 강화)
        if (!comment.getWriter().getMemberEmail().equals(requestDTO.getUser())) {
            throw new RuntimeException("본인이 작성한 댓글만 수정할 수 있습니다.");
        }

        // 내용 수정 후 저장
        comment.update(requestDTO.getComment());
        commentRepository.save(comment);
    }

    /**
     * ✅ 댓글 삭제
     */
    public void deleteComment(Long postId, Long commentId) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글이 존재하지 않습니다."));

        // 게시글 불일치 방지
        if (!comment.getPost().getId().equals(postId)) {
            throw new RuntimeException("해당 댓글은 이 게시글에 속하지 않습니다.");
        }

        commentRepository.delete(comment);
    }
}