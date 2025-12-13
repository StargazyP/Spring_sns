package kr.co.inhatc.inhatc.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import kr.co.inhatc.inhatc.dto.MemberDTO;
import kr.co.inhatc.inhatc.entity.MemberEntity;
import kr.co.inhatc.inhatc.repository.MemberRepository;
import kr.co.inhatc.inhatc.util.FileUploadValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${app.upload.profile-dir}")
    private String profileUploadDir;

    /**
     * 회원가입 - 비밀번호 암호화하여 저장
     */
    public void save(MemberDTO memberDTO) {
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(memberDTO.getMemberPassword());
        memberDTO.setMemberPassword(encodedPassword);
        
        MemberEntity entity = MemberEntity.fromDTO(memberDTO);
        memberRepository.save(entity);
        log.info("회원가입 완료: {}", memberDTO.getMemberEmail());
    }

    /**
     * 로그인 - 암호화된 비밀번호 검증
     * 기존 평문 비밀번호와의 호환성도 지원 (마이그레이션 기간)
     */
    public MemberDTO login(String email, String password) {
        Optional<MemberEntity> memberOpt = memberRepository.findByMemberEmail(email);
        
        if (memberOpt.isEmpty()) {
            log.warn("로그인 실패: 존재하지 않는 이메일 - {}", email);
            return null;
        }
        
        MemberEntity member = memberOpt.orElse(null);
        if (member == null) {
            return null;
        }
        
        String storedPassword = member.getMemberPassword();
        
        // BCrypt 해시로 시작하는지 확인 (암호화된 비밀번호)
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$")) {
            // 암호화된 비밀번호 검증
            if (passwordEncoder.matches(password, storedPassword)) {
                log.info("로그인 성공 (암호화): {}", email);
                return MemberEntity.toDTO(member);
            }
        } else {
            // 기존 평문 비밀번호와의 호환성 (마이그레이션 기간)
            if (storedPassword.equals(password)) {
                log.warn("평문 비밀번호 로그인 감지: {} - 암호화 마이그레이션 필요", email);
                // 자동으로 암호화하여 업데이트
                String encodedPassword = passwordEncoder.encode(password);
                member.setMemberPassword(encodedPassword);
                memberRepository.save(member);
                log.info("비밀번호 자동 암호화 완료: {}", email);
                return MemberEntity.toDTO(member);
            }
        }
        
        log.warn("로그인 실패: 비밀번호 불일치 - {}", email);
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
        // 공통 유틸리티 사용으로 중복 코드 제거
        Path filePath = kr.co.inhatc.inhatc.util.FileUploadService.uploadProfileImage(file, userEmail, profileUploadDir);

        Optional<MemberEntity> optionalMember = memberRepository.findByMemberEmail(userEmail);
        if (optionalMember.isPresent()) {
            MemberEntity member = optionalMember.get();
            // DB에는 전체 경로 저장
            member.setProfilePicturePath(filePath.toString());
            memberRepository.save(member);
            return String.format("파일 업로드 성공: %s", filePath.getFileName());
        } else {
            return "사용자를 찾을 수 없습니다.";
        }
    }
}