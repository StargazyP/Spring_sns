package kr.co.inhatc.inhatc;

import java.time.LocalDateTime;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import kr.co.inhatc.inhatc.dto.MessageDTO;
import kr.co.inhatc.inhatc.service.MessageService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketHandler {

    private final MessageService messageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ObjectNode sendMessage(@Payload ObjectNode message, SimpMessageHeaderAccessor headerAccessor) {
        String sender = message.get("sender").asText();
        String receiver = message.get("receiver").asText();
        String content = message.has("content") ? message.get("content").asText() : "";
        String imagePath = message.has("imagePath") && !message.get("imagePath").isNull() 
            ? message.get("imagePath").asText() : null;
        
        // 메시지 저장
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setSender(sender);
        messageDTO.setReceiver(receiver);
        messageDTO.setContent(content);
        messageDTO.setImagePath(imagePath);
        messageDTO.setTimestamp(LocalDateTime.now());
        messageService.saveMessage(messageDTO);
        
        ObjectNode response = objectMapper.createObjectNode();
        response.put("sender", sender);
        response.put("receiver", receiver);
        response.put("content", content);
        if (imagePath != null) {
            response.put("imagePath", imagePath);
        }
        response.put("timestamp", LocalDateTime.now().toString());
        
        return response;
    }

    @MessageMapping("/chat/{roomName}/sendMessage")
    @SendTo("/topic/{roomName}")
    public ObjectNode sendMessageToRoom(@Payload ObjectNode message, String roomName) {
        String sender = message.get("sender").asText();
        String receiver = message.get("receiver").asText();
        String content = message.has("content") ? message.get("content").asText() : "";
        String imagePath = message.has("imagePath") && !message.get("imagePath").isNull() 
            ? message.get("imagePath").asText() : null;
        
        // 메시지 저장
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setSender(sender);
        messageDTO.setReceiver(receiver);
        messageDTO.setContent(content);
        messageDTO.setImagePath(imagePath);
        messageDTO.setTimestamp(LocalDateTime.now());
        messageService.saveMessage(messageDTO);
        
        ObjectNode response = objectMapper.createObjectNode();
        response.put("sender", sender);
        response.put("receiver", receiver);
        response.put("content", content);
        if (imagePath != null) {
            response.put("imagePath", imagePath);
        }
        response.put("timestamp", LocalDateTime.now().toString());
        
        return response;
    }

    private String getNormalizedRoomName(String user1, String user2) {
        // 두 이메일을 알파벳순으로 정렬하여 동일한 룸 이름 생성
        return user1.compareTo(user2) < 0 ? user1 + "_" + user2 : user2 + "_" + user1;
    }
}

