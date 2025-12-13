package kr.co.inhatc.inhatc.repository;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.co.inhatc.inhatc.entity.CommentEntity;
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    // 특정 게시글의 모든 댓글 (최신순)
    // ✅ 안전: JPA 메서드 이름 기반 쿼리 (SQL Injection 위험 없음)
    List<CommentEntity> findByPostIdOrderByCreateDateDesc(Long postId);

    // 특정 회원 이메일로 작성된 댓글
    // ✅ 안전: JPA 메서드 이름 기반 쿼리 (SQL Injection 위험 없음)
    List<CommentEntity> findByWriterMemberEmail(String email);

    // ✅ N+1 문제 해결: JOIN FETCH를 사용하여 Writer, Post, ParentComment를 한 번에 조회
    @Query("SELECT DISTINCT c FROM CommentEntity c " +
           "LEFT JOIN FETCH c.writer " +
           "LEFT JOIN FETCH c.post " +
           "LEFT JOIN FETCH c.parentComment " +
           "WHERE c.post.id = :postId " +
           "ORDER BY c.createDate ASC")
    List<CommentEntity> findByPostIdWithWriter(@Param("postId") Long postId);

    // ✅ 페이징 지원: 특정 게시글의 댓글 조회 (페이징)
    @EntityGraph(attributePaths = {"writer", "post"})
    Page<CommentEntity> findByPostIdOrderByCreateDateDesc(Long postId, Pageable pageable);
}