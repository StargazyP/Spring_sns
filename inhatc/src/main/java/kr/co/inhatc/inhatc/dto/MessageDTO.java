package kr.co.inhatc.inhatc.dto;

import java.time.LocalDateTime;

public class MessageDTO {
    private String sender;
    private String receiver;
    private String content;
    private String imagePath; // 이미지 경로 필드 추가
    private LocalDateTime timestamp;

    // Getters and Setters

    public String getSender() {
        return sender;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
