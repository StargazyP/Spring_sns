# Docker 컨테이너 로그 확인 방법

## 컨테이너 정보
- **컨테이너 이름**: `spring-sns-app`
- **컨테이너 ID**: `9fc20bea1cd2`
- **상태**: `unhealthy` (건강 상태 불량)
- **포트**: `8080:8080`

## 로그 확인 방법

### 1. 기본 로그 확인 (최근 50줄)
```bash
docker logs spring-sns-app --tail 50
```

### 2. 전체 로그 확인
```bash
docker logs spring-sns-app
```

### 3. 실시간 로그 확인 (Follow 모드)
```bash
docker logs -f spring-sns-app
```
- `Ctrl + C`로 종료

### 4. 타임스탬프 포함 로그
```bash
docker logs -t spring-sns-app
```

### 5. 최근 N줄 + 실시간
```bash
docker logs -f --tail 100 spring-sns-app
```

### 6. 특정 시간 이후 로그
```bash
# 최근 10분간 로그
docker logs --since 10m spring-sns-app

# 최근 1시간 로그
docker logs --since 1h spring-sns-app

# 특정 시간 이후 (예: 2024-12-08T10:00:00)
docker logs --since "2024-12-08T10:00:00" spring-sns-app
```

### 7. 에러 로그만 확인
```bash
docker logs spring-sns-app 2>&1 | grep -i error
```

### 8. 특정 키워드 검색
```bash
# "Exception" 검색
docker logs spring-sns-app 2>&1 | grep -i exception

# "ERROR" 검색
docker logs spring-sns-app 2>&1 | grep -i error

# "WARN" 검색
docker logs spring-sns-app 2>&1 | grep -i warn
```

### 9. Docker Compose로 로그 확인
```bash
cd ~/포트폴리오/spring_sns_git/inhatc
docker compose logs app
docker compose logs -f app  # 실시간
docker compose logs --tail 100 app  # 최근 100줄
```

### 10. 모든 서비스 로그 확인
```bash
cd ~/포트폴리오/spring_sns_git/inhatc
docker compose logs  # 모든 서비스
docker compose logs -f  # 모든 서비스 실시간
```

## Unhealthy 상태 진단

### 1. Health Check 로그 확인
```bash
docker inspect spring-sns-app | grep -A 10 Health
```

### 2. Health Check 엔드포인트 직접 확인
```bash
curl http://localhost:8080/actuator/health
```

### 3. 컨테이너 상태 상세 확인
```bash
docker inspect spring-sns-app
```

### 4. 컨테이너 내부 접속
```bash
docker exec -it spring-sns-app sh
# 또는
docker exec -it spring-sns-app /bin/bash
```

## 유용한 조합 명령어

### 최근 에러 로그만 보기
```bash
docker logs spring-sns-app --tail 200 2>&1 | grep -i -E "error|exception|failed" | tail -20
```

### 로그를 파일로 저장
```bash
docker logs spring-sns-app > app-logs.txt 2>&1
```

### 로그를 파일로 저장하고 화면에도 출력
```bash
docker logs spring-sns-app 2>&1 | tee app-logs.txt
```

### 특정 시간대 로그 확인
```bash
docker logs --since "2024-12-08T00:00:00" --until "2024-12-08T23:59:59" spring-sns-app
```

## Spring Boot 특정 로그 확인

### 애플리케이션 시작 로그
```bash
docker logs spring-sns-app 2>&1 | grep -i "started"
```

### 데이터베이스 연결 로그
```bash
docker logs spring-sns-app 2>&1 | grep -i "datasource\|mysql\|jdbc"
```

### 포트 바인딩 로그
```bash
docker logs spring-sns-app 2>&1 | grep -i "port\|8080"
```

## 빠른 진단 스크립트

```bash
#!/bin/bash
# quick-diagnose.sh

echo "=== 컨테이너 상태 ==="
docker ps -a | grep spring-sns-app

echo ""
echo "=== 최근 에러 로그 (최근 50줄) ==="
docker logs spring-sns-app --tail 50 2>&1 | grep -i -E "error|exception|failed" | tail -20

echo ""
echo "=== Health Check ==="
curl -s http://localhost:8080/actuator/health || echo "Health endpoint 접근 실패"

echo ""
echo "=== 컨테이너 리소스 사용량 ==="
docker stats spring-sns-app --no-stream
```

## 문제 해결 팁

### 1. Unhealthy 상태인 경우
- Health Check 엔드포인트 확인: `/actuator/health`
- 애플리케이션이 정상적으로 시작되었는지 확인
- 데이터베이스 연결 확인
- 포트 충돌 확인

### 2. 로그가 너무 많은 경우
- `--tail` 옵션으로 최근 로그만 확인
- `grep`으로 필터링
- 로그 레벨 조정 (application.properties)

### 3. 로그가 보이지 않는 경우
- 컨테이너가 실행 중인지 확인: `docker ps`
- 컨테이너가 종료된 경우: `docker logs`로 종료 전 로그 확인

