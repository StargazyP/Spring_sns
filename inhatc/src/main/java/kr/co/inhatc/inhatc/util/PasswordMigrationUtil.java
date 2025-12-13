package kr.co.inhatc.inhatc.util;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import kr.co.inhatc.inhatc.entity.MemberEntity;
import kr.co.inhatc.inhatc.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 기존 평문 비밀번호를 암호화된 비밀번호로 마이그레이션하는 유틸리티
 * 
 * 사용 방법:
 * 1. 애플리케이션 시작 시 자동 실행 (권장하지 않음 - 데이터 손실 위험)
 * 2. 관리자 API 엔드포인트를 통해 수동 실행
 * 3. 별도 스크립트로 실행
 * 
 * 주의: 이 유틸리티는 평문 비밀번호를 암호화할 수 없습니다.
 * 사용자가 다음 로그인 시 자동으로 암호화됩니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PasswordMigrationUtil {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 모든 평문 비밀번호를 찾아서 로그에 기록
     * 실제 암호화는 사용자가 로그인할 때 자동으로 수행됩니다.
     * 
     * @return 평문 비밀번호를 가진 회원 수
     */
    public int findPlainTextPasswords() {
        List<MemberEntity> allMembers = memberRepository.findAll();
        int plainTextCount = 0;
        
        for (MemberEntity member : allMembers) {
            String password = member.getMemberPassword();
            // BCrypt 해시는 $2a$ 또는 $2b$로 시작
            if (password != null && !password.startsWith("$2a$") && !password.startsWith("$2b$")) {
                plainTextCount++;
                log.warn("평문 비밀번호 발견: {} (이메일: {})", 
                    member.getMemberName(), member.getMemberEmail());
            }
        }
        
        log.info("평문 비밀번호를 가진 회원 수: {}", plainTextCount);
        return plainTextCount;
    }

    /**
     * 특정 이메일의 비밀번호를 강제로 암호화
     * 주의: 이 메서드는 평문 비밀번호를 알 수 없으므로 사용할 수 없습니다.
     * 사용자는 다음 로그인 시 자동으로 암호화됩니다.
     * 
     * @param email 이메일
     * @param plainPassword 평문 비밀번호 (관리자가 알고 있는 경우에만 사용)
     * @return 성공 여부
     */
    public boolean migratePassword(String email, String plainPassword) {
        return memberRepository.findByMemberEmail(email)
            .map(member -> {
                String encodedPassword = passwordEncoder.encode(plainPassword);
                member.setMemberPassword(encodedPassword);
                memberRepository.save(member);
                log.info("비밀번호 마이그레이션 완료: {}", email);
                return true;
            })
            .orElse(false);
    }
}

