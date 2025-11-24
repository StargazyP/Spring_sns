package kr.co.inhatc.inhatc.dto;

import java.time.LocalDateTime;

import kr.co.inhatc.inhatc.entity.NotificationEntity;
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
public class NotificationDTO {
    private Long id;
    private Long postId;
    private String notificationType; // "LIKE" or "COMMENT"
    private String actorEmail;
    private String actorName;
    private String recipientEmail;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private String postContent; // 게시물 내용 (미리보기)

    public static NotificationDTO fromEntity(NotificationEntity notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .postId(notification.getPost().getId())
                .notificationType(notification.getNotificationType())
                .actorEmail(notification.getActorEmail())
                .recipientEmail(notification.getRecipientEmail())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .postContent(notification.getPost().getContent() != null && notification.getPost().getContent().length() > 50
                        ? notification.getPost().getContent().substring(0, 50) + "..."
                        : notification.getPost().getContent())
                .build();
    }
}

