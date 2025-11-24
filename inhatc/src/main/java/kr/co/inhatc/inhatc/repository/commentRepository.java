package kr.co.inhatc.inhatc.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.inhatc.inhatc.entity.CommentEntity;
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    // 특정 게시글의 모든 댓글 (최신순)
    List<CommentEntity> findByPostIdOrderByCreateDateDesc(Long postId);

    // 특정 회원 이메일로 작성된 댓글
    List<CommentEntity> findByWriterMemberEmail(String email);
}