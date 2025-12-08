package kr.co.inhatc.inhatc;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class InhatcApplicationTests {

	@Test
	void contextLoads() {
		// Application context 로드 테스트
		// 데이터베이스 연결 없이도 실행 가능하도록 설정 필요
	}

}
