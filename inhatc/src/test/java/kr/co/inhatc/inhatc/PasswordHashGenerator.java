package kr.co.inhatc.inhatc;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 비밀번호 BCrypt 해시 생성기
 * INSERT 쿼리용 해시 값을 생성합니다.
 */
public class PasswordHashGenerator {

    @Test
    public void generatePasswordHashes() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        System.out.println("=== 비밀번호 BCrypt 해시 생성 ===");
        System.out.println();
        
        System.out.println("비밀번호 '1234'의 해시:");
        String hash1234 = encoder.encode("1234");
        System.out.println(hash1234);
        System.out.println();
        
        System.out.println("비밀번호 '12345'의 해시:");
        String hash12345 = encoder.encode("12345");
        System.out.println(hash12345);
        System.out.println();
        
        System.out.println("=== INSERT 쿼리 ===");
        System.out.println();
        System.out.println("INSERT INTO member_entity (member_email, member_password, member_name) VALUES");
        System.out.println("('test@naver.com', '" + hash1234 + "', 'Joe'),");
        System.out.println("('guest@naver.com', '" + hash12345 + "', 'jang'),");
        System.out.println("('testing@naver.com', '" + hash1234 + "', 'jay'),");
        System.out.println("('main@naver.com', '" + hash1234 + "', 'choi'),");
        System.out.println("('jdajsl0415@naver.com', '$2a$10$LKmLYwHy/GDN00r.KFN8qOgZGKptl4ywdE0qYeqKRfTyKJp8puQVu', '장동건');");
    }
}

