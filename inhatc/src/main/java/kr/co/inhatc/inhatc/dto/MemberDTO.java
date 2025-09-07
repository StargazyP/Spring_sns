package kr.co.inhatc.inhatc.dto;

import java.util.Optional;

import kr.co.inhatc.inhatc.entity.MemberEntity;
import kr.co.inhatc.inhatc.repository.MemberRepository;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@NoArgsConstructor
@ToString
public class MemberDTO {

    private Long id;
    private String memberEmail;
    private String memberPassword;
    private String memberName;
    private String profilePicturePath; 

    public static MemberDTO toMemberDTO(MemberEntity memberEntity) {
        MemberDTO dto = new MemberDTO();
        dto.setId(memberEntity.getId());
        dto.setMemberEmail(memberEntity.getMemberEmail());
        dto.setMemberPassword(memberEntity.getMemberPassword());
        dto.setMemberName(memberEntity.getMemberName());
        dto.setProfilePicturePath(memberEntity.getProfilePicturePath());
        return dto;
    }



    public MemberDTO login(MemberDTO memberDTO, MemberRepository memberRepository) {
        Optional<MemberEntity> byMemberEmail = memberRepository.findByMemberEmail(memberDTO.getMemberEmail());
        if (byMemberEmail.isPresent()) {
            MemberEntity memberEntity = byMemberEmail.get();
            if (memberEntity.getMemberPassword().equals(memberDTO.getMemberPassword())) {
                return MemberDTO.toMemberDTO(memberEntity);
            } else {
                // 비밀번호가 일치하지 않음을 알림
                throw new RuntimeException("비밀번호가 일치하지 않습니다.");
            }
        } else {
            // 해당 이메일의 회원이 존재하지 않음을 알림
            throw new RuntimeException("해당 이메일로 가입된 회원이 없습니다.");
        }
    }

}
