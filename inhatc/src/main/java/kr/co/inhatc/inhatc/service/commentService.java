package kr.co.inhatc.inhatc.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import kr.co.inhatc.inhatc.dto.CommentRequestDTO;
import kr.co.inhatc.inhatc.dto.CommentResponseDTO;
import kr.co.inhatc.inhatc.entity.CommentEntity;
import kr.co.inhatc.inhatc.entity.MemberEntity;
import kr.co.inhatc.inhatc.entity.PostEntity;
import kr.co.inhatc.inhatc.repository.CommentRepository; // 수정된 부분: 대문자로 시작하는 클래스명으로 변경
import kr.co.inhatc.inhatc.repository.MemberRepository;
import kr.co.inhatc.inhatc.repository.PostRepository;

@Service
public class CommentService { // 수정된 부분: 클래스명은 대문자로 시작하는 명명 규칙에 따라 변경

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    public CommentService(CommentRepository commentRepository, MemberRepository memberRepository,
            PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.memberRepository = memberRepository;
        this.postRepository = postRepository;
    }
    
    public List<CommentResponseDTO> findByEmail(String email) {
        List<CommentEntity> list = commentRepository.findByWriterMemberEmail(email);
        return list.stream()
                   .map(CommentResponseDTO::new)
                   .collect(Collectors.toList());
    }
    
    

    public CommentResponseDTO addComment(CommentRequestDTO requestDTO) {
        // 댓글 작성자 정보 가져오기
        MemberEntity writer = memberRepository.findByMemberEmail(requestDTO.getUser())
                .orElseThrow(() -> new RuntimeException("댓글 작성자를 찾을 수 없습니다."));

        // 게시글 정보 가져오기
        PostEntity post = postRepository.findById(requestDTO.getArticle())
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 댓글 엔티티 생성
        CommentEntity comment = CommentEntity.builder()
                .comment(requestDTO.getComment())
                .writer(writer)
                .post(post)
                .build();

        // 댓글 저장 및 응답 DTO 반환
        return convertToDTO(commentRepository.save(comment));
    }

    public List<CommentResponseDTO> getCommentsByPostId(Long postId) {
        // 게시글에 속한 모든 댓글 가져오기
        List<CommentEntity> commentEntities = commentRepository.findByPostId(postId);

        // 댓글 엔티티를 DTO로 변환하여 반환
        return commentEntities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // CommentEntity를 CommentResponseDTO로 변환하는 메서드
    private CommentResponseDTO convertToDTO(CommentEntity entity) {
        return CommentResponseDTO.builder()
                .id(entity.getId())
                .post(entity.getPost().getId()) // 수정된 부분: 게시글 엔티티의 ID를 가져옴
                .comment(entity.getComment())
                .writer(entity.getWriter().getMemberEmail()) // 수정된 부분: 작성자 엔티티의 이메일을 가져옴
                .createdDate(entity.getCreateDate())
                .build();
    }
}
