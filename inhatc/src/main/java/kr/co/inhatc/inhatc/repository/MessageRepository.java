package kr.co.inhatc.inhatc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.co.inhatc.inhatc.entity.MessageEntity;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
    // 추가적인 쿼리 메서드가 필요하다면 정의합니다.
    List<MessageEntity> findBySenderAndReceiver(String sender, String receiver);

    List<MessageEntity> findByReceiverOrderById(String receiver);

    List<MessageEntity> findSenderByReceiver(String recevier);

    @Query("SELECT m FROM MessageEntity m WHERE m.receiver = :receiver OR m.sender = :receiver")
    List<MessageEntity> findByReceiverOrSender(@Param("receiver") String receiver);

    List<MessageEntity> findBySenderAndReceiverOrSenderAndReceiver(
            String sender1, String receiver1,
            String sender2, String receiver2);
}
