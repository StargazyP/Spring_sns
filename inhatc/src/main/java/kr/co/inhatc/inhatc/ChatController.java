package kr.co.inhatc.inhatc;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.co.inhatc.inhatc.dto.ChatMessageDTO;
import kr.co.inhatc.inhatc.dto.ChatRoomDTO;
import kr.co.inhatc.inhatc.dto.MessageDTO;
import kr.co.inhatc.inhatc.entity.ChatRoom;
import kr.co.inhatc.inhatc.repository.ChatRoomRepository;
import kr.co.inhatc.inhatc.repository.MemberRepository;
import kr.co.inhatc.inhatc.service.ChatRoomService;
import kr.co.inhatc.inhatc.service.MessageService;

@RestController
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MessageService messageService;

    @Autowired
    public ChatController(ChatRoomService chatRoomService,
            MemberRepository memberRepository,
            ChatRoomRepository chatRoomRepository,
            MessageService messageService) {
        this.chatRoomService = chatRoomService;
        this.memberRepository = memberRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.messageService = messageService;
    }

    @MessageMapping("/chat/{roomName}/sendMessage")
    @SendTo("/topic/{roomName}")
    public ChatMessageDTO sendMessage(@DestinationVariable String roomName, ChatMessageDTO messageDTO) {
        // 디버깅 메시지 추가
        System.out.println("Received message: " + messageDTO);
        
        // ChatMessageDTO를 MessageDTO로 변환
        MessageDTO message = new MessageDTO();
        message.setSender(messageDTO.getSender());
        message.setReceiver(messageDTO.getReceiver());
        message.setContent(messageDTO.getContent());
        message.setTimestamp(LocalDateTime.now()); // 타임스탬프 추가

        // 메시지를 저장
        messageService.saveMessage(message);
        
        // 디버깅: 저장된 메시지 출력
        System.out.println("Saved message: " + message);
        
        // 원래의 ChatMessageDTO 반환
        return messageDTO;
    }

    @MessageMapping("/chat/{receiverId}/start")
    public ChatRoomDTO startChat(@DestinationVariable String receiverId, SimpMessageHeaderAccessor headerAccessor) {
        String senderId = headerAccessor.getFirstNativeHeader("username");

        // 디버깅: senderId 확인
        System.out.println("Sender ID from header: " + senderId);

        if (senderId == null) {
            throw new IllegalArgumentException("Sender ID가 헤더에 없습니다.");
        }

        // 채팅방 생성 또는 기존 채팅방 반환
        ChatRoom chatRoom = chatRoomService.createChatRoom(senderId, receiverId);

        // 디버깅: 채팅방 정보 출력
        System.out.println("Chat room created or retrieved: " + chatRoom);

        return ChatRoomDTO.toChatRoomDTO(chatRoom);
    }

    // 두 사용자의 채팅 기록을 가져옵니다.
    @GetMapping("/api/chat/history")
    public List<MessageDTO> getChatHistory(@RequestParam String sender, @RequestParam String receiver) {
        // 디버깅: 전달받은 sender, receiver 값 확인
        System.out.println("Fetching chat history for sender: " + sender + " and receiver: " + receiver);
        
        List<MessageDTO> history = messageService.getMessageHistory(sender, receiver);
        
        // 디버깅: 채팅 기록 확인
        System.out.println("Chat history retrieved: " + history);
        
        return history;
    }

    // 특정 수신자에게 메시지를 보낸 발신자 리스트를 가져옵니다.
    @GetMapping("/api/chat/sender")
    public ResponseEntity<List<MessageDTO>> getSender(@RequestParam String receiver) {
        // 디버깅: 전달받은 receiver 값 확인
        System.out.println("Fetching senders for receiver: " + receiver);
        
        List<MessageDTO> senders = messageService.findMessagesByReceiverOrSender(receiver);
        
        // 디버깅: 발신자 리스트 확인
        System.out.println("Senders found: " + senders);
        
        return ResponseEntity.ok(senders);
    }
}
