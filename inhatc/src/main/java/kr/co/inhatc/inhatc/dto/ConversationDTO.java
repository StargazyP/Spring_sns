package kr.co.inhatc.inhatc.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationDTO {
    private String otherUserEmail; // 대화 상대 이메일
    private String otherUserName; // 대화 상대 이름
    private String lastMessage; // 최신 메시지 내용
    private LocalDateTime lastMessageTime; // 최신 메시지 시간
    private Long unreadCount; // 읽지 않은 메시지 수 (선택사항)
}

