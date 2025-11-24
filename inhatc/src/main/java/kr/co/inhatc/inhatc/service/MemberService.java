package kr.co.inhatc.inhatc.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import kr.co.inhatc.inhatc.dto.MemberDTO;
import kr.co.inhatc.inhatc.entity.MemberEntity;
import kr.co.inhatc.inhatc.repository.MemberRepository;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public void save(MemberDTO memberDTO) {
        MemberEntity entity = MemberEntity.fromDTO(memberDTO);
        memberRepository.save(entity);
    }

    public MemberDTO login(String email, String password) {
        Optional<MemberEntity> memberOpt = memberRepository.findByMemberEmail(email);
        if (memberOpt.isPresent() && memberOpt.get().getMemberPassword().equals(password)) {
            return MemberEntity.toDTO(memberOpt.get());
        }
        return null;
    }

    public MemberDTO getMemberByEmail(String email) {
        MemberEntity entity = memberRepository.findByMemberEmail(email)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));
        return MemberEntity.toDTO(entity);
    }

    /**
     * 이름으로 회원 조회 (첫 번째 일치하는 회원 반환)
     */
    public MemberDTO getMemberByName(String name) {
        List<MemberEntity> members = memberRepository.findByMemberName(name);
        if (members == null || members.isEmpty()) {
            throw new RuntimeException("회원이 존재하지 않습니다.");
        }
        // 첫 번째 일치하는 회원 반환
        return MemberEntity.toDTO(members.get(0));
    }

    public String storeFile(MultipartFile file, String userEmail) throws IOException {
        if (file.isEmpty()) return "파일이 비어 있습니다.";

        // 프로필 이미지 저장 경로: C:\Users\jdajs\spring test\inhatc\src\main\resources\static\{userEmail}\profile.png
        String baseDir = "C:/Users/jdajs/spring test/inhatc/src/main/resources/static";
        Path userDir = Paths.get(baseDir, userEmail);
        if (!Files.exists(userDir)) {
            Files.createDirectories(userDir);
        }

        // 파일 확장자 추출
        String originalFilename = file.getOriginalFilename();
        String extension = "png"; // 기본값
        if (originalFilename != null && originalFilename.lastIndexOf('.') != -1) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
        }

        String filename = "profile." + extension;
        Path filePath = userDir.resolve(filename);
        Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        Optional<MemberEntity> optionalMember = memberRepository.findByMemberEmail(userEmail);
        if (optionalMember.isPresent()) {
            MemberEntity member = optionalMember.get();
            // DB에는 전체 경로 저장
            member.setProfilePicturePath(filePath.toString());
            memberRepository.save(member);
            return "파일 업로드 성공: " + filename;
        } else {
            return "사용자를 찾을 수 없습니다.";
        }
    }
}