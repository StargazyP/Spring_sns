package kr.co.inhatc.inhatc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.inhatc.inhatc.entity.ChatHistoryEntity;

public interface ChatHistoryRepository extends JpaRepository<ChatHistoryEntity, Long> {
    
    
}
