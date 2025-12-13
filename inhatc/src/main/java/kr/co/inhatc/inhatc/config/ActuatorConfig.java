package kr.co.inhatc.inhatc.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.actuate.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Actuator 커스텀 설정
 * Health Check 및 Info 엔드포인트 커스터마이징
 */
@Configuration
public class ActuatorConfig {

    /**
     * 커스텀 Health Indicator
     * 애플리케이션 상태를 상세히 보고
     */
    @Bean
    public HealthIndicator customHealthIndicator() {
        return new HealthIndicator() {
            @Override
            public Health health() {
                // 추가적인 헬스 체크 로직을 여기에 구현
                // 예: 외부 서비스 연결 확인, 디스크 공간 확인 등
                
                Map<String, Object> details = new HashMap<>();
                details.put("status", "UP");
                details.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                details.put("application", "INHATC");
                
                return Health.up()
                        .withDetails(details)
                        .build();
            }
        };
    }

    /**
     * 커스텀 Info Contributor
     * 애플리케이션 정보 제공
     */
    @Bean
    public InfoContributor customInfoContributor() {
        return new InfoContributor() {
            @Override
            public void contribute(Info.Builder builder) {
                Map<String, Object> appInfo = new HashMap<>();
                appInfo.put("name", "INHATC");
                appInfo.put("description", "인하공전 소셜 네트워크 애플리케이션");
                appInfo.put("version", "1.0.0");
                appInfo.put("build-time", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                
                builder.withDetail("application", appInfo);
            }
        };
    }
}

