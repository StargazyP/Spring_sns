package kr.co.inhatc.inhatc.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import kr.co.inhatc.inhatc.dto.MemberDTO;
import kr.co.inhatc.inhatc.entity.MemberEntity;
import kr.co.inhatc.inhatc.repository.MemberRepository;
import lombok.RequiredArgsConstructor;


@Service // 스프링이 관리해주는 객체 == 스프링 빈
@RequiredArgsConstructor // controller와 같이. final 멤버변수 생성자 만드는 역할
public class MemberService {


    private final MemberRepository memberRepository; // 먼저 jpa, mysql dependency 추가

    public void save(MemberDTO memberDTO) {
        // repsitory의 save 메서드 호출
        MemberEntity memberEntity = MemberEntity.toMemberEntity(memberDTO);
        memberRepository.save(memberEntity);
        // Repository의 save메서드 호출 (조건. entity객체를 넘겨줘야 함)

    }


    public MemberDTO login(String memberEmail, String memberPassword) {
        MemberEntity loggedInMember = memberRepository.findByMemberEmail(memberEmail)
                .orElseThrow(() -> new RuntimeException("해당 이메일로 가입된 회원이 없습니다."));

        if (!loggedInMember.getMemberPassword().equals(memberPassword)) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return MemberDTO.toMemberDTO(loggedInMember);
    }

    public MemberDTO getMemberByEmail(String email) {
        MemberEntity memberEntity = memberRepository.findByMemberEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일로 가입된 회원이 없습니다."));
        return MemberDTO.toMemberDTO(memberEntity);
    }

    public String storeFile(MultipartFile file, String userEmail) throws IOException {
        if (file.isEmpty()) {
            return "업로드 실패: 파일이 비어 있습니다.";
        }

        String userDirectory = "C:\\Users\\jdajs\\Downloads\\spring test\\inhatc\\src\\main\\java\\kr\\" + userEmail; // 서버에 저장할 경로
        Path userPath = Path.of(userDirectory);
        if (!Files.exists(userPath)) {
            Files.createDirectories(userPath); // 사용자 디렉토리가 없다면 생성
        }

        // 파일 이름을 profile.png로 고정
        Path filePath = userPath.resolve("profile.png");
        
        // 이미 파일이 존재한다면 기존 파일을 덮어씁니다.
        Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        // Optional<MemberEntity> 사용
        Optional<MemberEntity> optionalMember = memberRepository.findByMemberEmail(userEmail);
        if (optionalMember.isPresent()) {
            MemberEntity member = optionalMember.get();
            member.setProfilePicturePath(filePath.toString());
            memberRepository.save(member); // 변경 사항을 저장
            return "파일이 성공적으로 업로드 되었습니다: profile.png";
        } else {
            return "사용자를 찾을 수 없습니다.";
        }
    }

}
// MemberService.class