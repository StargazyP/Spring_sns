package kr.co.inhatc.inhatc.service;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.co.inhatc.inhatc.entity.ChatRoom;
import kr.co.inhatc.inhatc.repository.ChatRoomRepository;
import kr.co.inhatc.inhatc.repository.MemberRepository;

@Service
public class ChatRoomService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private MemberRepository memberRepository;

    // 채팅방 생성 또는 기존 채팅방 반환
    public ChatRoom createChatRoom(String senderId, String receiverId) {
        String roomName = getNormalizedRoomName(senderId, receiverId);
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByName(roomName);
        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setName(roomName);
        chatRoom.setParticipants(Arrays.asList(
            memberRepository.findByMemberEmail(senderId).orElseThrow(),
            memberRepository.findByMemberEmail(receiverId).orElseThrow()
        ));
        System.out.println("Creating new chat room: " + chatRoom);
        return chatRoomRepository.save(chatRoom);
    }

    // 채팅방 이름을 정규화
    private String getNormalizedRoomName(String userId1, String userId2) {
        return Stream.of(userId1, userId2)
                .sorted()
                .collect(Collectors.joining("_"));
    }

    // 특정 채팅방 찾기
    public ChatRoom findChatRoomByName(String name) {
        return chatRoomRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + name));
    }
}
