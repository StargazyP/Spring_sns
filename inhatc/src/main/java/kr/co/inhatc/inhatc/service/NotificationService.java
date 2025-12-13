package kr.co.inhatc.inhatc.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import kr.co.inhatc.inhatc.dto.NotificationDTO;
import kr.co.inhatc.inhatc.entity.MemberEntity;
import kr.co.inhatc.inhatc.entity.NotificationEntity;
import kr.co.inhatc.inhatc.entity.PostEntity;
import kr.co.inhatc.inhatc.repository.MemberRepository;
import kr.co.inhatc.inhatc.repository.NotificationRepository;
import kr.co.inhatc.inhatc.repository.PostRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    /**
     * 알림 생성 (좋아요)
     */
    public void createLikeNotification(Long postId, String actorEmail) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 본인 게시물에 좋아요를 누른 경우 알림 생성하지 않음
        if (post.getMemberEmail().equals(actorEmail)) {
            return;
        }

        // 이미 같은 사용자가 같은 게시물에 좋아요 알림을 보낸 경우 중복 방지
        boolean exists = notificationRepository.findByPostId(postId).stream()
                .anyMatch(n -> n.getActorEmail().equals(actorEmail) 
                        && n.getNotificationType().equals("LIKE")
                        && !n.getIsRead());

        if (!exists) {
            NotificationEntity notification = NotificationEntity.builder()
                    .post(post)
                    .notificationType("LIKE")
                    .actorEmail(actorEmail)
                    .recipientEmail(post.getMemberEmail())
                    .build();

            notificationRepository.save(notification);
        }
    }

    /**
     * 알림 생성 (댓글)
     */
    public void createCommentNotification(Long postId, String actorEmail) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 본인 게시물에 댓글을 남긴 경우 알림 생성하지 않음
        if (post.getMemberEmail().equals(actorEmail)) {
            return;
        }

        NotificationEntity notification = NotificationEntity.builder()
                .post(post)
                .notificationType("COMMENT")
                .actorEmail(actorEmail)
                .recipientEmail(post.getMemberEmail())
                .build();

        notificationRepository.save(notification);
    }

    /**
     * 사용자의 읽지 않은 알림 조회
     */
    public List<NotificationDTO> getUnreadNotifications(String recipientEmail) {
        List<NotificationEntity> notifications = notificationRepository
                .findByRecipientEmailAndIsReadFalseOrderByCreatedAtDesc(recipientEmail);

        return notifications.stream()
                .map(notification -> {
                    NotificationDTO dto = NotificationDTO.fromEntity(notification);
                    // actorName 설정
                    memberRepository.findByMemberEmail(notification.getActorEmail())
                            .ifPresent(member -> dto.setActorName(member.getMemberName()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 모든 알림 조회 (읽음/안읽음 모두)
     */
    public List<NotificationDTO> getAllNotifications(String recipientEmail) {
        List<NotificationEntity> notifications = notificationRepository
                .findByRecipientEmailOrderByCreatedAtDesc(recipientEmail);

        return notifications.stream()
                .map(notification -> {
                    NotificationDTO dto = NotificationDTO.fromEntity(notification);
                    // actorName 설정
                    memberRepository.findByMemberEmail(notification.getActorEmail())
                            .ifPresent(member -> dto.setActorName(member.getMemberName()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * 알림 읽음 처리
     */
    public void markAsRead(Long notificationId) {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("알림을 찾을 수 없습니다."));
        notification.markAsRead();
        notificationRepository.save(notification);
    }

    /**
     * 모든 알림 읽음 처리
     */
    public void markAllAsRead(String recipientEmail) {
        List<NotificationEntity> notifications = notificationRepository
                .findByRecipientEmailAndIsReadFalseOrderByCreatedAtDesc(recipientEmail);
        notifications.forEach(NotificationEntity::markAsRead);
        notificationRepository.saveAll(notifications);
    }
}

