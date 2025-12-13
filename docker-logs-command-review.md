# Docker 로그 명령어 검토

## 📋 사용한 명령어

```bash
sudo docker logs --tail 200 -f 4471a012328e
```

## ✅ 명령어 검토 결과

### 명령어 구조 분석

```bash
sudo docker logs --tail 200 -f 4471a012328e
│    │     │     │       │  │
│    │     │     │       │  └─ 컨테이너 ID
│    │     │     │       └─ follow 모드 (실시간 로그)
│    │     │     └─ 최근 200줄만 표시
│    │     └─ logs 명령어
│    └─ docker 명령어
└─ sudo (관리자 권한)
```

### ✅ 올바른 부분

1. **`docker logs`**: 로그 확인 명령어 ✅
2. **`--tail 200`**: 최근 200줄 표시 ✅
3. **`-f`**: 실시간 로그 추적 (follow 모드) ✅
4. **컨테이너 ID**: `4471a012328e` ✅

### ⚠️ 개선 가능한 부분

1. **`sudo` 사용**: 
   - 현재 사용자가 `docker` 그룹에 속해있다면 `sudo` 불필요
   - `sudo` 없이도 작동할 수 있음

2. **컨테이너 이름 사용 권장**:
   - 컨테이너 ID 대신 이름 사용이 더 명확함
   - 예: `docker logs spring-sns-app --tail 200 -f`

## 🔍 권장 명령어

### 방법 1: 컨테이너 이름 사용 (권장)

```bash
# sudo 없이
docker logs spring-sns-app --tail 200 -f

# 또는
docker logs --tail 200 -f spring-sns-app
```

### 방법 2: Docker Compose 사용

```bash
cd ~/포트폴리오/spring_sns_git/inhatc
docker compose logs -f --tail 200 app
```

### 방법 3: 컨테이너 ID 사용 (현재 방식)

```bash
# sudo 없이 시도
docker logs --tail 200 -f 4471a012328e

# sudo 필요 시
sudo docker logs --tail 200 -f 4471a012328e
```

## 📊 로그 분석

### 발견된 에러

1. **SocketTimeoutException**
   - 클라이언트 연결 타임아웃
   - 네트워크 문제 또는 응답 지연

2. **HttpMessageNotWritableException**
   - 이미지 요청(`image/png`)에 대한 에러 응답을 JSON으로 변환 실패
   - Content-Type 불일치 문제

3. **정적 리소스 없음**
   - `test@naver.com/profile.png` 파일을 찾을 수 없음
   - 정상적인 경고 (파일이 없을 수 있음)

## ✅ 최종 권장 명령어

### 가장 간단한 방법

```bash
# 컨테이너 이름 사용
docker logs spring-sns-app --tail 200 -f
```

### Docker Compose 사용 (권장)

```bash
cd ~/포트폴리오/spring_sns_git/inhatc
docker compose logs -f --tail 200 app
```

### 추가 옵션

```bash
# 타임스탬프 포함
docker logs -t --tail 200 -f spring-sns-app

# 특정 시간 이후 로그
docker logs --since 10m -f spring-sns-app

# 에러만 필터링
docker logs --tail 200 spring-sns-app 2>&1 | grep -i error
```

## 🔧 sudo 필요 여부 확인

```bash
# docker 그룹 확인
groups | grep docker

# 또는
id -nG | grep docker
```

**결과**:
- `docker` 그룹에 속해있으면: `sudo` 불필요
- 속해있지 않으면: `sudo` 필요 또는 `docker` 그룹 추가

## 📝 요약

### 현재 명령어
```bash
sudo docker logs --tail 200 -f 4471a012328e
```

**평가**: ✅ **기능적으로는 올바름**

### 개선 제안
```bash
# 더 명확한 방법
docker logs spring-sns-app --tail 200 -f

# 또는 Docker Compose 사용
cd ~/포트폴리오/spring_sns_git/inhatc
docker compose logs -f --tail 200 app
```

---

**검토 일시**: 2025-12-13  
**명령어 상태**: ✅ 정상 작동 (개선 가능)

