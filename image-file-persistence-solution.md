# 이미지 파일 영구 저장 해결 방안

## 문제 상황

Webhook으로 pull하고 Docker 컨테이너를 재시작하면 이미지 파일들이 404 에러로 로드되지 않는 문제가 발생했습니다.

### 원인
1. **Docker 컨테이너 재시작 시 파일 시스템 초기화**: 컨테이너 내부에 저장된 파일이 사라짐
2. **볼륨 마운트 없음**: 호스트 파일 시스템과 연결되지 않음
3. **하드코딩된 경로**: ImageController에서 Windows 경로 하드코딩

---

## 해결 방안

### 1. Docker Compose 볼륨 마운트 추가

`docker-compose.yml`에 볼륨 마운트를 추가하여 호스트의 업로드 디렉토리를 컨테이너와 공유:

```yaml
services:
  app:
    volumes:
      # 업로드된 이미지 파일 영구 저장 (호스트와 공유)
      - ./uploads:/app/uploads
      # static 리소스도 마운트 (기존 파일 유지)
      - ./src/main/resources/static:/app/src/main/resources/static:ro
    environment:
      # 업로드 디렉토리 경로 (Docker 환경)
      - UPLOAD_BASE_DIR=/app/uploads
      - UPLOAD_POSTS_DIR=/app/uploads/posts
      - UPLOAD_PROFILE_DIR=/app/uploads/static
      - UPLOAD_MESSAGES_DIR=/app/uploads/static
```

### 2. 업로드 디렉토리 생성

호스트에 업로드 디렉토리 생성:

```bash
mkdir -p /home/jangdonggun/포트폴리오/spring_sns_git/inhatc/uploads/{posts,static}
```

### 3. ImageController 수정

하드코딩된 Windows 경로를 제거하고 설정에서 가져온 경로 사용:

```java
@Value("${app.upload.profile-dir}")
private String profileUploadDir;

@GetMapping("/{email}/{filename:.+}")
public ResponseEntity<Resource> getImage(
        @PathVariable String email,
        @PathVariable String filename) throws MalformedURLException {
    
    Path file = Paths.get(profileUploadDir, email, filename);
    // ...
}
```

### 4. 기존 파일 복사

기존 static 디렉토리의 파일을 uploads 디렉토리로 복사:

```bash
cp -r src/main/resources/static/* uploads/static/
```

---

## 적용된 변경 사항

### docker-compose.yml
- ✅ 볼륨 마운트 추가 (`./uploads:/app/uploads`)
- ✅ static 리소스 마운트 추가 (읽기 전용)
- ✅ 환경 변수로 업로드 경로 설정

### ImageController.java
- ✅ 하드코딩된 경로 제거
- ✅ `@Value`로 설정에서 경로 가져오기
- ✅ 파일 확장자에 따른 Content-Type 처리

### 업로드 디렉토리 구조
```
inhatc/
├── uploads/
│   ├── posts/          # 게시물 이미지
│   └── static/          # 프로필 및 메시지 이미지
│       ├── test@naver.com/
│       │   └── profile.png
│       └── ...
└── src/main/resources/static/  # 정적 리소스 (읽기 전용)
```

---

## 배포 후 확인 사항

### 1. 볼륨 마운트 확인
```bash
docker exec spring-sns-app ls -la /app/uploads
```

### 2. 환경 변수 확인
```bash
docker exec spring-sns-app env | grep UPLOAD
```

### 3. 이미지 파일 접근 테스트
```bash
curl -I http://localhost:8080/static/test@naver.com/profile.png
curl -I http://localhost:8080/posts/test@naver.com/20251213191915833.png
```

---

## 주의 사항

1. **권한 설정**: 업로드 디렉토리에 쓰기 권한이 필요합니다
   ```bash
   chmod -R 755 uploads/
   ```

2. **기존 파일 백업**: 기존 static 디렉토리의 파일을 uploads로 복사해야 합니다

3. **Webhook 재배포**: 변경사항을 커밋하고 푸시하면 자동으로 재배포됩니다

---

## 다음 단계

1. 변경사항 커밋 및 푸시
2. Webhook으로 자동 배포 확인
3. 이미지 파일 접근 테스트
4. 컨테이너 재시작 후 파일 유지 확인

---

**해결 완료**: 2025-12-13  
**상태**: ✅ 볼륨 마운트 설정 완료

