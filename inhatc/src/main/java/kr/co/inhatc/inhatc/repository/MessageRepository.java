package kr.co.inhatc.inhatc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.co.inhatc.inhatc.entity.MessageEntity;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
    // 추가적인 쿼리 메서드가 필요하다면 정의합니다.
    // ✅ 안전: JPA 메서드 이름 기반 쿼리 (SQL Injection 위험 없음)
    List<MessageEntity> findBySenderAndReceiver(String sender, String receiver);

    // ✅ 안전: JPA 메서드 이름 기반 쿼리 (SQL Injection 위험 없음)
    List<MessageEntity> findByReceiverOrderById(String receiver);

    // ✅ 안전: JPA 메서드 이름 기반 쿼리 (SQL Injection 위험 없음)
    List<MessageEntity> findSenderByReceiver(String recevier);

    // ✅ 안전: JPQL + @Param 사용 (SQL Injection 위험 없음)
    // ⚠️ 주의: Native Query 사용 시 반드시 @Param 사용 필수
    @Query("SELECT m FROM MessageEntity m WHERE m.receiver = :receiver OR m.sender = :receiver")
    List<MessageEntity> findByReceiverOrSender(@Param("receiver") String receiver);

    // ✅ 안전: JPA 메서드 이름 기반 쿼리 (SQL Injection 위험 없음)
    List<MessageEntity> findBySenderAndReceiverOrSenderAndReceiver(
            String sender1, String receiver1,
            String sender2, String receiver2);
    
    // 특정 사용자와 메시지를 주고받은 상대방 목록 조회 (최신 메시지 순)
    // ✅ 안전: JPQL + @Param 사용 (SQL Injection 위험 없음)
    // ⚠️ 주의: Native Query 사용 시 반드시 @Param 사용 필수
    @Query("SELECT m FROM MessageEntity m WHERE (m.sender = :userEmail OR m.receiver = :userEmail) " +
           "ORDER BY m.timestamp DESC")
    List<MessageEntity> findConversationsByUserEmail(@Param("userEmail") String userEmail);
}
