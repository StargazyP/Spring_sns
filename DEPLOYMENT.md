# 배포 설정 가이드

## GitHub Secrets 설정

이 프로젝트의 CI/CD 파이프라인을 사용하려면 다음 Secrets를 GitHub에 설정해야 합니다.

### 필수 Secrets

#### 1. Docker Hub 인증 정보
- **DOCKER_USERNAME**: Docker Hub 사용자명
- **DOCKER_PASSWORD**: Docker Hub 비밀번호 또는 Access Token

#### 2. SSH 배포 정보
- **SSH_HOST**: `jangdonggun.iptime.org`
- **SSH_USERNAME**: 서버 사용자명 (예: `ubuntu`, `jangdonggun`)
- **SSH_PRIVATE_KEY**: SSH 개인 키 전체 내용
- **SSH_PORT**: SSH 포트 (기본값: `22`)

## SSH 키 설정 방법

### 1. SSH 키 생성 (로컬에서)

```bash
# SSH 키 생성
ssh-keygen -t rsa -b 4096 -C "github-actions-deploy" -f ~/.ssh/github_actions_deploy

# 개인 키 확인 (GitHub Secrets에 복사)
cat ~/.ssh/github_actions_deploy
```

### 2. 공개 키를 서버에 추가

```bash
# 서버에 SSH 접속
ssh ubuntu@jangdonggun.iptime.org

# authorized_keys에 공개 키 추가
echo "공개키내용" >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
```

또는 로컬에서:

```bash
# 공개 키를 서버에 복사
ssh-copy-id -i ~/.ssh/github_actions_deploy.pub ubuntu@jangdonggun.iptime.org
```

### 3. GitHub Secrets에 추가

1. GitHub 저장소 → Settings → Secrets and variables → Actions
2. "New repository secret" 클릭
3. 다음 값들을 추가:

```
SSH_HOST = jangdonggun.iptime.org
SSH_USERNAME = ubuntu (또는 실제 사용자명)
SSH_PRIVATE_KEY = -----BEGIN OPENSSH PRIVATE KEY-----
                  ... (전체 개인 키 내용)
                  -----END OPENSSH PRIVATE KEY-----
SSH_PORT = 22
```

## 배포 서버 준비

### 1. 서버에 프로젝트 디렉토리 생성

```bash
ssh ubuntu@jangdonggun.iptime.org

# 디렉토리 생성
mkdir -p ~/포트폴리오/Spring_sns
cd ~/포트폴리오/Spring_sns
```

### 2. docker-compose.yml 파일 준비

서버에 `docker-compose.yml` 파일이 있어야 합니다. 
GitHub Actions가 다음 경로를 확인합니다:
- `/home/ubuntu/포트폴리오/Spring_sns`
- 또는 `/home/ubuntu/spring_sns_git/inhatc`

### 3. Docker 및 Docker Compose 설치 확인

```bash
# Docker 설치 확인
docker --version
docker compose version
```

## 배포 프로세스

1. **코드 푸시**: `git push origin main`
2. **자동 빌드**: GitHub Actions가 자동으로 빌드 및 테스트
3. **Docker 이미지 푸시**: Docker Hub에 이미지 업로드
4. **자동 배포**: SSH로 서버 접속 후 `docker compose up -d --build` 실행

## 배포 확인

### GitHub Actions에서
- Actions 탭에서 "Deploy to Server" Job 확인
- 로그에서 배포 성공 여부 확인

### 서버에서
```bash
ssh ubuntu@jangdonggun.iptime.org
cd ~/포트폴리오/Spring_sns
docker compose ps
docker compose logs -f app
```

### 애플리케이션 확인
```bash
# 헬스체크
curl http://jangdonggun.iptime.org:8080/actuator/health

# 또는 브라우저에서
http://jangdonggun.iptime.org:8080
```

## 문제 해결

### SSH 연결 실패
- SSH 키가 올바르게 설정되었는지 확인
- 서버의 `~/.ssh/authorized_keys`에 공개 키가 추가되었는지 확인
- 방화벽에서 SSH 포트(22)가 열려있는지 확인

### 배포 실패
- 서버에 docker-compose.yml 파일이 있는지 확인
- Docker 및 Docker Compose가 설치되어 있는지 확인
- 서버의 디스크 공간 확인

### 컨테이너 실행 실패
```bash
# 로그 확인
docker compose logs app

# 컨테이너 재시작
docker compose restart app
```

