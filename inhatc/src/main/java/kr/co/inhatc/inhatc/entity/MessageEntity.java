package kr.co.inhatc.inhatc.entity;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
@Entity
@Getter
@Setter
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sender;
    private String receiver;
    private String content;
    private String imagePath; // 이미지 경로 필드 추가
    private LocalDateTime timestamp; // 타임스탬프 필드 추가

    // 기본 생성자 및 setter, getter는 @Getter, @Setter로 처리됨
}