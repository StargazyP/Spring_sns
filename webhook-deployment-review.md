# Webhook 배포 검토 보고서

## 📊 현재 상태 요약

### ✅ 정상 작동 중인 항목
1. **Git 동기화**: 로컬과 원격 저장소 완전 동기화
   - 로컬 HEAD: `f740bd1` (fix test case)
   - 원격 origin/main: `f740bd1` (fix test case)
   - **상태**: ✅ 완전히 동기화됨

2. **Webhook 서버**: 정상 작동 중
   - PM2 상태: `online`
   - Health Check: ✅ 정상 응답
   - 포트: 3000
   - **상태**: ✅ 정상 작동

3. **GitHub 저장소**: 최신 상태
   - 최신 커밋: `f740bd1` (fix test case)
   - **상태**: ✅ 최신

### ⚠️ 주의가 필요한 항목

1. **Docker 이미지**: 4일 전 빌드 (2025-12-09)
   - 현재 이미지: `stargazyp2/spring-sns:latest` (a9f92e1211a0)
   - 생성일: 2025-12-09 06:00:22
   - **문제**: 최신 소스코드 변경사항이 이미지에 반영되지 않았을 수 있음

2. **컨테이너 상태**: Unhealthy
   - 컨테이너: `spring-sns-app`
   - 상태: `Up 4 hours (unhealthy)`
   - **문제**: Health check 실패

## 🔍 상세 분석

### 1. Git 동기화 상태
```
로컬 HEAD:    f740bd1e59cbe6d494cf9e982b1df73f68a48930
원격 origin:  f740bd1e59cbe6d494cf9e982b1df73f68a48930
```
**결과**: ✅ 완전히 동기화됨

### 2. Webhook 서버 상태
- **PM2 상태**: online
- **Health Check**: `{"status":"ok","timestamp":"2025-12-13T18:28:45.846Z"}`
- **포트**: 3000
- **결과**: ✅ 정상 작동

### 3. Docker 이미지 상태
- **이미지**: `stargazyp2/spring-sns:latest`
- **이미지 ID**: `a9f92e1211a0`
- **생성일**: 2025-12-09 06:00:22 (4일 전)
- **문제**: 
  - 최신 소스코드 커밋: `f740bd1` (fix test case)
  - 이미지가 4일 전에 빌드됨
  - **최신 코드 변경사항이 이미지에 반영되지 않았을 가능성**

### 4. 컨테이너 상태
- **컨테이너**: `spring-sns-app`
- **상태**: `Up 4 hours (unhealthy)`
- **이미지**: `stargazyp2/spring-sns:latest`
- **문제**: Health check 실패

## 🚨 발견된 문제점

### 문제 1: Docker 이미지가 오래됨
**원인 분석**:
- GitHub Actions가 최신 커밋에 대해 이미지를 빌드하지 않았거나
- Webhook이 트리거되지 않았거나
- Docker Hub에 푸시되지 않았을 수 있음

**확인 필요**:
1. GitHub Actions의 최근 워크플로우 실행 이력
2. Docker Hub의 최신 이미지 태그 확인
3. Webhook이 실제로 트리거되었는지 확인

### 문제 2: 컨테이너가 Unhealthy 상태
**원인 분석**:
- Health check 엔드포인트 (`/actuator/health`) 접근 실패
- 애플리케이션 시작 실패
- 의존성 문제 (MySQL 연결 등)

**확인 필요**:
1. Health check 엔드포인트 직접 확인
2. 애플리케이션 로그 확인
3. MySQL 연결 상태 확인

## ✅ 해결 방안

### 방안 1: 최신 이미지로 업데이트 (권장)

```bash
# 1. 최신 이미지 Pull
docker pull stargazyp2/spring-sns:latest

# 2. 컨테이너 재시작
cd ~/포트폴리오/spring_sns_git/inhatc
docker compose pull app
docker compose up -d --force-recreate app

# 3. 상태 확인
docker compose ps
docker compose logs -f app
```

### 방안 2: GitHub Actions 수동 트리거

1. GitHub 저장소로 이동
2. Actions 탭 클릭
3. 최근 워크플로우 확인
4. 필요시 수동으로 재실행

### 방안 3: Webhook 수동 트리거 테스트

```bash
# Webhook 테스트
curl -X POST http://localhost:3000/webhook \
  -H "Content-Type: application/json" \
  -H "X-GitHub-Event: push" \
  -d '{"ref":"refs/heads/main","head_commit":{"id":"f740bd1"}}'
```

### 방안 4: 로컬에서 직접 빌드 및 배포

```bash
cd ~/포트폴리오/spring_sns_git/inhatc

# 1. 최신 코드 Pull (이미 동기화됨)
git pull origin main

# 2. 로컬에서 빌드
docker compose build app

# 3. 재시작
docker compose up -d app
```

## 📋 체크리스트

### 즉시 확인 필요
- [ ] GitHub Actions의 최근 워크플로우 실행 확인
- [ ] Docker Hub에서 최신 이미지 확인
- [ ] Health check 엔드포인트 직접 테스트
- [ ] 애플리케이션 로그에서 에러 확인

### 조치 사항
- [ ] 최신 이미지로 업데이트
- [ ] 컨테이너 재시작
- [ ] Health check 문제 해결
- [ ] Webhook 로그 확인

## 🔧 진단 명령어

### 1. 최신 이미지 확인
```bash
docker pull stargazyp2/spring-sns:latest
docker images stargazyp2/spring-sns:latest
```

### 2. Health Check 테스트
```bash
curl http://localhost:8080/actuator/health
```

### 3. 컨테이너 상태 확인
```bash
docker compose ps
docker inspect spring-sns-app | grep -A 10 Health
```

### 4. Webhook 로그 확인
```bash
pm2 logs webhook-server --lines 50
```

### 5. 애플리케이션 로그 확인
```bash
docker logs spring-sns-app --tail 100
```

## 📝 결론

### 현재 상태
- ✅ **Git 동기화**: 완벽하게 동기화됨
- ✅ **Webhook 서버**: 정상 작동
- ⚠️ **Docker 이미지**: 4일 전 빌드 (최신 코드 반영 여부 불확실)
- ⚠️ **컨테이너**: Unhealthy 상태

### 권장 조치
1. **즉시**: 최신 이미지로 업데이트 및 컨테이너 재시작
2. **확인**: GitHub Actions 워크플로우 실행 이력 확인
3. **모니터링**: Health check 문제 해결 및 로그 모니터링

### 예상 시나리오
- **시나리오 1**: GitHub Actions가 최신 커밋에 대해 이미지를 빌드하지 않음
  - **해결**: 수동으로 워크플로우 트리거 또는 로컬 빌드
- **시나리오 2**: 이미지가 빌드되었지만 Webhook이 트리거되지 않음
  - **해결**: Webhook 수동 트리거 또는 docker compose pull
- **시나리오 3**: 이미지가 최신이지만 컨테이너가 오래된 이미지를 사용 중
  - **해결**: docker compose pull && docker compose up -d

