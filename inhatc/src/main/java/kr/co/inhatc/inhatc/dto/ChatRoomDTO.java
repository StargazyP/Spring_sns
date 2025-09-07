package kr.co.inhatc.inhatc.dto;

import java.util.List;
import java.util.stream.Collectors;

import kr.co.inhatc.inhatc.entity.ChatRoom;
import kr.co.inhatc.inhatc.entity.MemberEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ChatRoomDTO {

    private Long id;
    private String name;
    private List<MemberDTO> participants;

    public static ChatRoomDTO toChatRoomDTO(ChatRoom chatRoom) {
        ChatRoomDTO dto = new ChatRoomDTO();
        dto.setId(chatRoom.getId());
        dto.setName(chatRoom.getName());

        List<MemberDTO> participantDTOs = chatRoom.getParticipants().stream()
                .map(MemberDTO::toMemberDTO)
                .collect(Collectors.toList());
        dto.setParticipants(participantDTOs);

        return dto;
    }

    public static ChatRoom toChatRoomEntity(ChatRoomDTO chatRoomDTO, List<MemberEntity> participants) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setId(chatRoomDTO.getId());
        chatRoom.setName(chatRoomDTO.getName());
        chatRoom.setParticipants(participants);

        return chatRoom;
    }
}
