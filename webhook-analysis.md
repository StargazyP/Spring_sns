# Webhook 배포 시스템 분석 보고서

## 🔍 현재 상태 분석

### 1. Git 동기화 상태
- **최신 커밋**: `f740bd1` - "fix test case" (2025-12-14 02:52:29 KST)
- **로컬과 원격**: ✅ 완전히 동기화됨
- **상태**: 최신 코드 반영됨

### 2. Docker 이미지 상태
- **이미지**: `stargazyp2/spring-sns:latest`
- **이미지 ID**: `1c6068f7cc1d`
- **생성일**: 2025-12-13 17:54:49 UTC (한국 시간: 2025-12-14 02:54:49)
- **상태**: ✅ 최신 커밋(f740bd1) 이후에 빌드됨

### 3. 컨테이너 상태
- **컨테이너**: `spring-sns-app`
- **재시작 시간**: 2025-12-13 18:29:48 (한국 시간: 2025-12-14 03:29:48)
- **상태**: ✅ 최신 이미지 사용 중

### 4. Webhook 서버
- **상태**: ✅ 정상 작동 중
- **로그**: "배포 완료!" 메시지 확인됨

## ⚠️ 발견된 문제점

### 문제: Webhook이 Git Pull을 하지 않음

현재 `webhook-server.js`의 배포 로직을 보면:

```javascript
deployCommand += `
  if [ -d "${path}" ]; then
    cd "${path}" && 
    docker compose pull app &&
    docker compose up -d --build &&
    ...
  fi
`;
```

**문제점**:
1. ❌ `git pull` 명령어가 없음
2. ❌ 로컬 코드가 최신이 아닐 수 있음
3. ❌ Docker Hub의 이미지만 Pull하고 있음

### 현재 배포 흐름

```
GitHub Push
  ↓
GitHub Actions (Docker 이미지 빌드)
  ↓
Docker Hub에 푸시
  ↓
Webhook 트리거
  ↓
서버에서 docker compose pull (Docker Hub에서 이미지만 가져옴)
  ↓
컨테이너 재시작
```

**문제**: 로컬 Git 저장소를 업데이트하지 않음

## ✅ 해결 방안

### 방안 1: Webhook에 Git Pull 추가 (권장)

`webhook-server.js`를 수정하여 Git Pull을 추가:

```javascript
deployCommand += `
  if [ -d "${path}" ]; then
    cd "${path}" && 
    echo "📥 최신 코드 가져오기..." &&
    git fetch origin &&
    git reset --hard origin/main &&
    git pull origin main &&
    echo "🐳 Docker 이미지 업데이트..." &&
    docker compose pull app &&
    docker compose up -d --build &&
    ...
  fi
`;
```

### 방안 2: 상위 디렉토리에서 Git Pull

`inhatc` 디렉토리는 Git 저장소가 아니므로, 상위 디렉토리(`spring_sns_git`)에서 Pull:

```javascript
const possiblePaths = [
  {
    gitPath: '/home/jangdonggun/포트폴리오/spring_sns_git',
    composePath: '/home/jangdonggun/포트폴리오/spring_sns_git/inhatc'
  }
];

// Git Pull은 상위 디렉토리에서
cd "${gitPath}" && git pull origin main

// Docker Compose는 하위 디렉토리에서
cd "${composePath}" && docker compose up -d --build
```

## 📊 현재 작동 방식 분석

### 실제로 작동하는 부분

1. ✅ **GitHub Actions**: 코드 변경 시 Docker 이미지 자동 빌드
2. ✅ **Docker Hub**: 이미지 자동 푸시
3. ✅ **Webhook 트리거**: GitHub Actions에서 Webhook 호출
4. ✅ **Docker Pull**: 최신 이미지 Pull
5. ✅ **컨테이너 재시작**: 최신 이미지로 재시작

### 작동하지 않는 부분

1. ❌ **로컬 Git 업데이트**: Webhook이 로컬 저장소를 업데이트하지 않음
2. ⚠️ **로컬 빌드**: 로컬에서 빌드하는 경우 최신 코드가 반영되지 않을 수 있음

## 🎯 결론

### 현재 상태

**Webhook은 부분적으로 작동하고 있습니다:**

✅ **작동하는 부분**:
- GitHub에서 Docker Hub로 이미지 배포
- Webhook 트리거
- Docker 이미지 Pull 및 컨테이너 재시작

❌ **작동하지 않는 부분**:
- 로컬 Git 저장소 업데이트
- 로컬 코드 동기화

### 권장 조치

1. **즉시**: Webhook 서버에 Git Pull 로직 추가
2. **확인**: 로컬 저장소가 최신인지 정기적으로 확인
3. **모니터링**: Webhook 로그에서 Git Pull 성공 여부 확인

### 수정된 Webhook 코드 예시

```javascript
deployCommand += `
  if [ -d "${path}" ]; then
    # 상위 디렉토리에서 Git Pull
    cd "$(dirname "${path}")" &&
    echo "📥 최신 코드 가져오기..." &&
    git fetch origin &&
    git reset --hard origin/main &&
    git pull origin main &&
    # Docker Compose 디렉토리로 이동
    cd "${path}" &&
    echo "🐳 Docker 이미지 업데이트..." &&
    docker compose pull app 2>/dev/null || docker-compose pull app 2>/dev/null &&
    docker compose up -d --build 2>/dev/null || docker-compose up -d --build 2>/dev/null &&
    docker compose ps 2>/dev/null || docker-compose ps 2>/dev/null &&
    echo "✅ 배포 완료!" &&
    exit 0
  fi
`;
```

---

**분석 일시**: 2025-12-13 18:36  
**결론**: Webhook은 Docker 이미지 배포는 잘 작동하지만, 로컬 Git 동기화가 필요함

