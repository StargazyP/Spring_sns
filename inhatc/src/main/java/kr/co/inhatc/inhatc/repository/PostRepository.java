package kr.co.inhatc.inhatc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kr.co.inhatc.inhatc.entity.PostEntity;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, Long> {

    // 삭제되지 않은 게시글 조회
    List<PostEntity> findByDeleteYnOrderByCreatedDateDesc(char deleteYn);

    // 회원 이메일 기준 조회
    List<PostEntity> findByMemberEmailOrderByIdDesc(String memberEmail);

    // 회원 이름 기준 조회는 Repository에서 바로 불가, Service에서 Member 조회 후 email로 검색
    // 필요 시 아래처럼 커스텀 Query 작성 가능
    @Query("SELECT p FROM PostEntity p WHERE p.memberEmail IN :emails ORDER BY p.id DESC")
    List<PostEntity> findByMemberEmails(@Param("emails") List<String> emails);

}
