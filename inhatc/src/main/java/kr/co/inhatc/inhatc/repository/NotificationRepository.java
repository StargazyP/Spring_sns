package kr.co.inhatc.inhatc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kr.co.inhatc.inhatc.entity.NotificationEntity;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    
    // 특정 사용자의 읽지 않은 알림 조회 (최신순)
    List<NotificationEntity> findByRecipientEmailAndIsReadFalseOrderByCreatedAtDesc(String recipientEmail);
    
    // 특정 사용자의 모든 알림 조회 (최신순)
    List<NotificationEntity> findByRecipientEmailOrderByCreatedAtDesc(String recipientEmail);
    
    // 특정 게시물의 알림 조회
    List<NotificationEntity> findByPostId(Long postId);
}

