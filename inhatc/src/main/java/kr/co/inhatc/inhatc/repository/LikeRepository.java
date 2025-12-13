package kr.co.inhatc.inhatc.repository;

import kr.co.inhatc.inhatc.entity.LikeEntity;
import kr.co.inhatc.inhatc.entity.MemberEntity;
import kr.co.inhatc.inhatc.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<LikeEntity, Long> {
    // ✅ 안전: JPA 메서드 이름 기반 쿼리 (SQL Injection 위험 없음)
    Optional<LikeEntity> findByPostAndUser(PostEntity post, MemberEntity user);

    // ✅ 안전: JPA 메서드 이름 기반 쿼리 (SQL Injection 위험 없음)
    List<LikeEntity> findByPost(PostEntity post);

    // ✅ 안전: JPA 메서드 이름 기반 쿼리 (SQL Injection 위험 없음)
    List<LikeEntity> findByPostIn(List<PostEntity> posts);

    
}
