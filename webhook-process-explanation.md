# Webhook 배포 프로세스 상세 설명

## 🔄 전체 배포 프로세스

### 단계별 흐름

```
1. 개발자가 GitHub에 Push
   ↓
2. GitHub Actions 자동 트리거
   ↓
3. 빌드 및 테스트 (Build and Test)
   ↓
4. Docker 이미지 빌드 및 Docker Hub 푸시
   ↓
5. Webhook 트리거 (서버에 배포 요청)
   ↓
6. 서버에서 Docker 이미지 Pull
   ↓
7. Docker 컨테이너 재시작
   ↓
8. 최신 코드가 적용된 애플리케이션 실행 ✅
```

## 📋 상세 단계 설명

### 1단계: GitHub에 Push
```bash
git add .
git commit -m "변경사항"
git push origin main
```

**트리거 조건**:
- `main` 또는 `develop` 브랜치에 Push
- Pull Request 생성

### 2단계: GitHub Actions 자동 트리거

**워크플로우 파일**: `.github/workflows/ci-cd.yml`

**트리거 조건**:
```yaml
on:
  push:
    branches:
      - main
      - develop
  pull_request:
    branches:
      - main
      - develop
```

### 3단계: 빌드 및 테스트 (Build and Test)

**작업 내용**:
1. 코드 체크아웃
2. JDK 19 설정
3. Maven 빌드 (`mvn clean package -DskipTests`)
4. 테스트 실행 (`mvn test`)
5. JAR 파일 아티팩트 업로드

**소요 시간**: 약 2-5분

### 4단계: Docker 이미지 빌드 및 Docker Hub 푸시

**작업 내용**:
1. Docker Buildx 설정
2. Docker Hub 로그인
3. Docker 이미지 빌드 (Multi-stage build)
4. Docker Hub에 푸시 (`stargazyp2/spring-sns:latest`)

**소요 시간**: 약 3-5분

**조건**: `main` 또는 `develop` 브랜치에 Push한 경우만 실행

### 5단계: Webhook 트리거

**작업 내용**:
1. GitHub Actions가 Webhook 서버에 HTTP POST 요청
2. 요청 URL: `http://서버주소:3000/webhook`
3. 헤더: `X-GitHub-Event: push`
4. 본문: 커밋 정보 포함

**소요 시간**: 약 1-2초

**조건**: `main` 브랜치에 Push한 경우만 실행

### 6단계: 서버에서 Docker 이미지 Pull

**작업 내용** (Webhook 서버에서 실행):
1. 프로젝트 디렉토리로 이동
2. `docker compose pull app` 실행
3. Docker Hub에서 최신 이미지 다운로드

**소요 시간**: 약 10-30초

### 7단계: Docker 컨테이너 재시작

**작업 내용**:
1. `docker compose up -d --build` 실행
2. 기존 컨테이너 중지
3. 최신 이미지로 새 컨테이너 시작
4. Health Check 대기

**소요 시간**: 약 30초-1분

### 8단계: 애플리케이션 실행

**결과**:
- 최신 코드가 적용된 Spring Boot 애플리케이션 실행
- Health Check 통과
- 서비스 정상 제공

## ⏱️ 전체 소요 시간

| 단계 | 소요 시간 |
|------|----------|
| 빌드 및 테스트 | 2-5분 |
| Docker 이미지 빌드 및 푸시 | 3-5분 |
| Webhook 트리거 | 1-2초 |
| 서버 배포 | 1-2분 |
| **총 소요 시간** | **약 6-12분** |

## 🔍 실제 워크플로우 확인

### GitHub Actions 워크플로우 구조

```yaml
jobs:
  build-and-test:      # 1. 빌드 및 테스트
    - Maven 빌드
    - 테스트 실행
    - JAR 파일 생성
  
  build-docker-image:  # 2. Docker 이미지 빌드
    - Docker 이미지 빌드
    - Docker Hub에 푸시
  
  deploy:              # 3. 배포
    - Webhook 트리거 (우선)
    - SSH 배포 (Fallback)
```

### Webhook 배포 단계

```yaml
- name: Deploy via Webhook (Fast)
  run: |
    curl -X POST http://서버:3000/webhook \
      -H "X-GitHub-Event: push" \
      -d '{"ref":"refs/heads/main"}'
```

### Webhook 서버 처리

```javascript
app.post('/webhook', (req, res) => {
  if (event === 'push') {
    // 1. 프로젝트 디렉토리로 이동
    cd /home/jangdonggun/포트폴리오/spring_sns_git/inhatc
    
    // 2. Docker 이미지 Pull
    docker compose pull app
    
    // 3. 컨테이너 재시작
    docker compose up -d --build
    
    // 4. 상태 확인
    docker compose ps
  }
});
```

## ✅ 확인 사항

### 정상 작동 확인

1. **GitHub Actions 실행 확인**
   - GitHub 저장소 → Actions 탭에서 확인
   - 모든 Job이 성공적으로 완료되어야 함

2. **Docker Hub 이미지 확인**
   - Docker Hub에서 `stargazyp2/spring-sns:latest` 확인
   - 최신 이미지가 업데이트되었는지 확인

3. **Webhook 서버 로그 확인**
   ```bash
   pm2 logs webhook-server
   ```
   - "배포 완료!" 메시지 확인

4. **컨테이너 상태 확인**
   ```bash
   docker compose ps
   ```
   - 컨테이너가 최신 이미지로 재시작되었는지 확인

5. **애플리케이션 Health Check**
   ```bash
   curl http://localhost:8080/actuator/health
   ```
   - `{"status":"UP"}` 응답 확인

## 🎯 요약

### 질문: "GitHub에 push하면 빌드 테스팅하고 webhook으로 docker container에 적용되는거지?"

**답변**: ✅ **맞습니다!**

**전체 프로세스**:
1. ✅ GitHub에 Push
2. ✅ GitHub Actions가 자동으로 빌드 및 테스트 실행
3. ✅ Docker 이미지 빌드 및 Docker Hub에 푸시
4. ✅ Webhook이 서버에 배포 요청
5. ✅ 서버에서 Docker 이미지 Pull 및 컨테이너 재시작
6. ✅ 최신 코드가 적용된 애플리케이션 실행

**자동화 수준**: 완전 자동화 (Push만 하면 자동으로 배포됨)

**소요 시간**: 약 6-12분

**장점**:
- 코드 변경 후 자동으로 테스트 실행
- 테스트 통과 시 자동 배포
- 수동 작업 불필요
- 빠른 배포 (Webhook 사용 시)

---

**작성일**: 2025-12-13  
**프로젝트**: Spring SNS  
**배포 방식**: GitHub Actions + Docker Hub + Webhook

