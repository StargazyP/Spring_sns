# Webhook 즉각 반영 설정 가이드

## 설정 완료된 내용

### 1. Webhook 서버 (`webhook-server.js`)
- ✅ Express 서버로 webhook 수신
- ✅ GitHub push 이벤트 감지
- ✅ 자동으로 Docker 이미지 pull 및 재시작
- ✅ 여러 디렉토리 경로 자동 탐색

### 2. GitHub Actions 워크플로우 수정
- ✅ Webhook 호출 추가 (빠른 배포)
- ✅ SSH 배포를 Fallback으로 유지

## 서버에서 Webhook 서버 실행하기

### 방법 1: 스크립트 사용 (권장)

```bash
# 서버에 접속
ssh jangdonggun@jangdonggun.iptime.org

# 스크립트 실행
cd ~/포트폴리오/spring_sns_git
bash webhook-setup.sh
```

### 방법 2: 수동 설정

```bash
# 서버 접속
ssh jangdonggun@jangdonggun.iptime.org

# Node.js 설치 (없는 경우)
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt-get install -y nodejs

# 패키지 설치
cd ~/포트폴리오/spring_sns_git
npm install express

# PM2 설치 (프로세스 관리)
sudo npm install -g pm2

# Webhook 서버 시작
pm2 start webhook-server.js --name webhook-server
pm2 save
pm2 startup
```

## GitHub Secrets 설정

GitHub Repository → Settings → Secrets and variables → Actions에서 추가:

1. **WEBHOOK_SECRET** (선택사항)
   - Webhook 서명 검증용 시크릿
   - 설정하지 않으면 검증 건너뜀

## 작동 방식

### Git Push 시 흐름

```
1. git push origin main
   ↓
2. GitHub Actions 트리거
   ↓
3. 빌드 및 테스트
   ↓
4. Docker 이미지 빌드 및 Docker Hub 푸시
   ↓
5. Webhook 호출 (즉각 반영!)
   → http://jangdonggun.iptime.org:3000/webhook
   → 서버에서 자동으로 docker compose pull & up
   ↓
6. (Webhook 실패 시) SSH 배포 (Fallback)
```

## Webhook 서버 관리

```bash
# 상태 확인
pm2 status

# 로그 확인
pm2 logs webhook-server

# 재시작
pm2 restart webhook-server

# 중지
pm2 stop webhook-server

# 시작
pm2 start webhook-server

# 삭제
pm2 delete webhook-server
```

## 포트 확인

Webhook 서버는 **포트 3000**에서 실행됩니다.

```bash
# 포트 확인
netstat -tlnp | grep 3000
# 또는
ss -tlnp | grep 3000

# 방화벽 설정 (필요시)
sudo ufw allow 3000/tcp
```

## Health Check

Webhook 서버 상태 확인:
```bash
curl http://localhost:3000/health
```

## 문제 해결

### Webhook이 작동하지 않는 경우
1. 서버에서 webhook 서버가 실행 중인지 확인
   ```bash
   pm2 status
   ```

2. 포트가 열려있는지 확인
   ```bash
   netstat -tlnp | grep 3000
   ```

3. 로그 확인
   ```bash
   pm2 logs webhook-server
   ```

4. SSH Fallback으로 자동 전환됨 (워크플로우에 설정됨)

## 장점

✅ **즉각 반영**: Docker Hub 푸시 후 즉시 서버에 반영
✅ **빠른 배포**: SSH 접속보다 빠름
✅ **Fallback 지원**: Webhook 실패 시 SSH 배포로 자동 전환
✅ **자동화**: 수동 개입 없이 완전 자동 배포

