package kr.co.inhatc.inhatc.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kr.co.inhatc.inhatc.entity.PostEntity;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, Long> {

    // 삭제되지 않은 게시글 조회
    // ✅ 안전: JPA 메서드 이름 기반 쿼리 (SQL Injection 위험 없음)
    List<PostEntity> findByDeleteYnOrderByCreatedDateDesc(char deleteYn);

    // 회원 이메일 기준 조회
    // ✅ 안전: JPA 메서드 이름 기반 쿼리 (SQL Injection 위험 없음)
    List<PostEntity> findByMemberEmailOrderByIdDesc(String memberEmail);

    // 회원 이름 기준 조회는 Repository에서 바로 불가, Service에서 Member 조회 후 email로 검색
    // ✅ 안전: JPQL + @Param 사용 (SQL Injection 위험 없음)
    // ⚠️ 주의: Native Query 사용 시 반드시 @Param 사용 필수
    @Query("SELECT p FROM PostEntity p WHERE p.memberEmail IN :emails ORDER BY p.id DESC")
    List<PostEntity> findByMemberEmails(@Param("emails") List<String> emails);

    // ✅ N+1 문제 해결: JOIN FETCH를 사용하여 Comments를 한 번에 조회
    // 주의: Member는 memberEmail로만 연결되어 있어 JOIN FETCH 불가, Service에서 별도 조회 필요
    @Query("SELECT DISTINCT p FROM PostEntity p " +
           "LEFT JOIN FETCH p.comments " +
           "WHERE p.deleteYn = :deleteYn " +
           "ORDER BY p.createdDate DESC")
    List<PostEntity> findAllWithCommentsByDeleteYn(@Param("deleteYn") char deleteYn);

    // ✅ N+1 문제 해결: 특정 이메일의 게시글을 Comments와 함께 조회
    @Query("SELECT DISTINCT p FROM PostEntity p " +
           "LEFT JOIN FETCH p.comments " +
           "WHERE p.memberEmail = :memberEmail " +
           "ORDER BY p.id DESC")
    List<PostEntity> findByMemberEmailWithComments(@Param("memberEmail") String memberEmail);

    // ✅ N+1 문제 해결: 여러 이메일의 게시글을 Comments와 함께 조회
    @Query("SELECT DISTINCT p FROM PostEntity p " +
           "LEFT JOIN FETCH p.comments " +
           "WHERE p.memberEmail IN :emails " +
           "ORDER BY p.id DESC")
    List<PostEntity> findByMemberEmailsWithComments(@Param("emails") List<String> emails);

    // ✅ 페이징 지원: 삭제되지 않은 게시글 조회 (페이징)
    // @EntityGraph 제거: 페이징과 Collection fetch 충돌 방지
    // @BatchSize로 N+1 문제 해결
    Page<PostEntity> findByDeleteYnOrderByCreatedDateDesc(char deleteYn, Pageable pageable);

    // ✅ 페이징 지원: 특정 이메일의 게시글 조회 (페이징)
    // @EntityGraph 제거: 페이징과 Collection fetch 충돌 방지
    // @BatchSize로 N+1 문제 해결
    Page<PostEntity> findByMemberEmailOrderByIdDesc(String memberEmail, Pageable pageable);

}
