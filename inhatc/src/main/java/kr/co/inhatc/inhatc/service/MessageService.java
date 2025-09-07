package kr.co.inhatc.inhatc.service;

import java.util.List;
import java.util.stream.Collectors;

// import org.hibernate.mapping.List;
import org.springframework.stereotype.Service;

import kr.co.inhatc.inhatc.dto.MessageDTO;
import kr.co.inhatc.inhatc.entity.MessageEntity;
import kr.co.inhatc.inhatc.repository.MessageRepository;
@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public MessageEntity saveMessage(MessageDTO messageDTO) {
        MessageEntity message = new MessageEntity();
        message.setSender(messageDTO.getSender());
        message.setReceiver(messageDTO.getReceiver());
        message.setContent(messageDTO.getContent());
        message.setTimestamp(messageDTO.getTimestamp()); // 타임스탬프 설정
        System.out.println("Saving message: " + message);
        return messageRepository.save(message);
    }
    
        // 메시지 히스토리 조회
        public List<MessageDTO> getMessageHistory(String sender, String receiver) {
            // 발신자와 수신자의 순서에 관계없이 메시지를 가져옵니다.
            List<MessageEntity> messages = messageRepository.findBySenderAndReceiverOrSenderAndReceiver(
                sender, receiver, 
                receiver, sender);
                System.out.println("Fetching message history for sender: " + sender + " and receiver: " + receiver);
                return messages.stream().map(message -> {
                MessageDTO dto = new MessageDTO();
                dto.setSender(message.getSender());
                dto.setReceiver(message.getReceiver());
                dto.setContent(message.getContent());
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
        System.out.println("Finding messages for receiver: " + receiver);
        return messages.stream().map(message -> {
            MessageDTO dto = new MessageDTO();
            dto.setSender(message.getSender());
            dto.setReceiver(message.getReceiver());
            return dto;
        }).collect(Collectors.toList());
    }
}
