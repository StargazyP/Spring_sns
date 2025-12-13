package kr.co.inhatc.inhatc.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// import org.hibernate.mapping.List;
import org.springframework.stereotype.Service;

import kr.co.inhatc.inhatc.dto.ConversationDTO;
import kr.co.inhatc.inhatc.dto.MessageDTO;
import kr.co.inhatc.inhatc.entity.MessageEntity;
import kr.co.inhatc.inhatc.repository.MessageRepository;
import kr.co.inhatc.inhatc.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final MemberRepository memberRepository;

    public MessageService(MessageRepository messageRepository, MemberRepository memberRepository) {
        this.messageRepository = messageRepository;
        this.memberRepository = memberRepository;
    }

    public MessageEntity saveMessage(MessageDTO messageDTO) {
        MessageEntity message = new MessageEntity();
        message.setSender(messageDTO.getSender());
        message.setReceiver(messageDTO.getReceiver());
        message.setContent(messageDTO.getContent());
        message.setImagePath(messageDTO.getImagePath()); // 이미지 경로 설정
        message.setTimestamp(messageDTO.getTimestamp()); // 타임스탬프 설정
        log.debug("메시지 저장: sender={}, receiver={}", message.getSender(), message.getReceiver());
        return messageRepository.save(message);
    }
    
        // 메시지 히스토리 조회
        public List<MessageDTO> getMessageHistory(String sender, String receiver) {
            // 발신자와 수신자의 순서에 관계없이 메시지를 가져옵니다.
            List<MessageEntity> messages = messageRepository.findBySenderAndReceiverOrSenderAndReceiver(
                sender, receiver, 
                receiver, sender);
                log.debug("메시지 히스토리 조회: sender={}, receiver={}, 메시지 수={}", sender, receiver, messages.size());
                return messages.stream().map(message -> {
                MessageDTO dto = new MessageDTO();
                dto.setSender(message.getSender());
                dto.setReceiver(message.getReceiver());
                dto.setContent(message.getContent());
                dto.setImagePath(message.getImagePath()); // 이미지 경로 설정
                dto.setTimestamp(message.getTimestamp());
                return dto;
            }).collect(Collectors.toList());
        }

    // 사용자에게 받은 모든 메시지 조회 (ID별 정렬)
    public List<MessageDTO> getReceivedMessagesSortedById(String receiver) {
        List<MessageEntity> messages = messageRepository.findByReceiverOrderById(receiver);
        
        return messages.stream().map(message -> {
            MessageDTO dto = new MessageDTO();
            dto.setSender(message.getSender());
            dto.setReceiver(message.getReceiver());
            dto.setContent(message.getContent());
            return dto;
        }).collect(Collectors.toList());
    }

    public List<MessageDTO> findMessagesByReceiverOrSender(String receiver) {
        List<MessageEntity> messages = messageRepository.findByReceiverOrSender(receiver);
        log.debug("메시지 조회: receiver={}, 메시지 수={}", receiver, messages.size());
        return messages.stream().map(message -> {
            MessageDTO dto = new MessageDTO();
            dto.setSender(message.getSender());
            dto.setReceiver(message.getReceiver());
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 세션 유저의 message_entity를 조회하여 대화 상대 목록을 반환
     * - userEmail이 sender 또는 receiver인 모든 메시지를 조회
     * - 상대방별로 그룹화하여 최신 메시지만 유지
     * - 최신 메시지 시간 순으로 정렬하여 반환
     * 
     * @param userEmail 세션 유저의 이메일 (senderEmail)
     * @return 대화 상대 목록 (ConversationDTO 리스트)
     */
    public List<ConversationDTO> getConversations(String userEmail) {
        log.debug("대화 상대 목록 조회: userEmail={}", userEmail);
        
        List<MessageEntity> messages = messageRepository.findConversationsByUserEmail(userEmail);
        log.debug("조회된 메시지 수: {}", messages.size());
        
        if (messages.isEmpty()) {
            log.debug("메시지가 없음: userEmail={}", userEmail);
            return new ArrayList<>();
        }
        
        // 상대방 이메일별로 그룹화하고 최신 메시지만 유지
        Map<String, MessageEntity> conversationMap = new LinkedHashMap<>();
        
        for (MessageEntity message : messages) {
            String otherUserEmail = message.getSender().equals(userEmail) 
                ? message.getReceiver() 
                : message.getSender();
            
            // 이미 존재하지 않거나 더 최신 메시지인 경우 업데이트
            if (!conversationMap.containsKey(otherUserEmail)) {
                conversationMap.put(otherUserEmail, message);
            } else {
                MessageEntity existingMessage = conversationMap.get(otherUserEmail);
                if (message.getTimestamp() != null && existingMessage.getTimestamp() != null &&
                    message.getTimestamp().isAfter(existingMessage.getTimestamp())) {
                    conversationMap.put(otherUserEmail, message);
                } else if (message.getTimestamp() == null && existingMessage.getTimestamp() == null) {
                    // 둘 다 timestamp가 null이면 ID가 큰 것을 사용
                    if (message.getId() != null && existingMessage.getId() != null &&
                        message.getId() > existingMessage.getId()) {
                        conversationMap.put(otherUserEmail, message);
                    }
                }
            }
        }
        
        log.debug("대화 상대 수: {}", conversationMap.size());
        
        // ConversationDTO로 변환
        List<ConversationDTO> conversations = new ArrayList<>();
        for (Map.Entry<String, MessageEntity> entry : conversationMap.entrySet()) {
            String otherUserEmail = entry.getKey();
            MessageEntity lastMessage = entry.getValue();
            
            // 사용자 정보 조회
            String otherUserName = otherUserEmail;
            var memberOptional = memberRepository.findByMemberEmail(otherUserEmail);
            if (memberOptional.isPresent()) {
                otherUserName = memberOptional.get().getMemberName();
            } else {
                log.warn("사용자 정보 없음: {}", otherUserEmail);
            }
            
            ConversationDTO conversation = ConversationDTO.builder()
                .otherUserEmail(otherUserEmail)
                .otherUserName(otherUserName)
                .lastMessage(lastMessage.getContent())
                .lastMessageTime(lastMessage.getTimestamp())
                .unreadCount(0L) // 읽지 않은 메시지 수는 나중에 구현 가능
                .build();
            
            conversations.add(conversation);
        }
        
        // 최신 메시지 시간 순으로 정렬
        conversations.sort((a, b) -> {
            if (a.getLastMessageTime() == null && b.getLastMessageTime() == null) return 0;
            if (a.getLastMessageTime() == null) return 1;
            if (b.getLastMessageTime() == null) return -1;
            return b.getLastMessageTime().compareTo(a.getLastMessageTime());
        });
        
        log.debug("최종 대화 상대 수: {}", conversations.size());
        
        return conversations;
    }
}
