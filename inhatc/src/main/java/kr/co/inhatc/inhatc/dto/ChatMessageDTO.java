package kr.co.inhatc.inhatc.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageDTO {
    private String sender;
    private String receiver;
    private String content;
    private LocalDateTime timestamp;
    public ChatMessageDTO() {}

    public ChatMessageDTO(String sender, String receiver, String content, LocalDateTime timestamp){
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = timestamp;

    }
    
}
