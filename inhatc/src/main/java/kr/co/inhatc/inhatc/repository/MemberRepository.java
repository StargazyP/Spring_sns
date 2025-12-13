package kr.co.inhatc.inhatc.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // Repository 어노테이션을 임포트해야 합니다.

import kr.co.inhatc.inhatc.entity.MemberEntity; // MemberEntity 클래스를 임포트해야 합니다.

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    // ✅ 안전: JPA 메서드 이름 기반 쿼리 (SQL Injection 위험 없음)
    Optional<MemberEntity> findByMemberEmail(String memberEmail);

    // ✅ 안전: JPA 메서드 이름 기반 쿼리 (SQL Injection 위험 없음)
    List<MemberEntity> findByMemberName(String memberName);

    // ✅ N+1 문제 해결: 여러 이메일의 회원을 한 번에 조회
    List<MemberEntity> findByMemberEmailIn(List<String> memberEmails);

}
