package kr.co.inhatc.inhatc.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // Repository 어노테이션을 임포트해야 합니다.

import kr.co.inhatc.inhatc.entity.MemberEntity; // MemberEntity 클래스를 임포트해야 합니다.

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    Optional<MemberEntity> findByMemberEmail(String memberEmail);

    List<MemberEntity> findByMemberName(String memberName);

}
