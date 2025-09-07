package kr.co.inhatc.inhatc.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.inhatc.inhatc.entity.CommentEntity;
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    List<CommentEntity> findByPostId(Long postId); // 게시글 ID로 댓글 조회
    List<CommentEntity> findByWriterMemberEmail(String memberEmail); // 작성자 이메일로 댓글 조회
}
