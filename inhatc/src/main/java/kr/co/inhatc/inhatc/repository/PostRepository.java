package kr.co.inhatc.inhatc.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.inhatc.inhatc.entity.PostEntity;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    /*
     * 
     * 유저 아이디로 게시물 불러오기
     */

    List<PostEntity> findByWriter(String writer);

    /**
     * 게시글 리스트 조회 
     */
    List<PostEntity> findAllByDeleteYn(final char deleteYn, final Sort sort);



}