package kr.co.inhatc.inhatc.repository;

import kr.co.inhatc.inhatc.entity.LikeEntity;
import kr.co.inhatc.inhatc.entity.MemberEntity;
import kr.co.inhatc.inhatc.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<LikeEntity, Long> {
    Optional<LikeEntity> findByPostAndUser(PostEntity post, MemberEntity user);

    List<LikeEntity> findByPost(PostEntity post);

    List<LikeEntity> findByPostIn(List<PostEntity> posts);

    
}
