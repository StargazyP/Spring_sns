#  소셜 미디어 플랫폼 (X/Twitter 스타일)

Spring Boot 기반의 실시간 소셜 미디어 플랫폼입니다. 사용자 간 게시글 공유, 댓글, 좋아요, 실시간 채팅 등의 기능을 제공합니다.

##  목차

- [프로젝트 소개](#프로젝트-소개)
- [주요 기능](#주요-기능)
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [데이터베이스 설계](#데이터베이스-설계)
- [실행 방법](#실행-방법)
- [API 문서](#api-문서)
- [주요 화면](#주요-화면)

---

##  프로젝트 소개

이 프로젝트는 **Spring Boot 3.3.5**와 **MySQL**을 활용하여 개발된 풀스택 소셜 미디어 애플리케이션입니다. X(Twitter)의 UI/UX를 참고하여 직관적인 인터페이스를 제공하며, **WebSocket**을 통한 실시간 채팅 기능을 구현했습니다.

### 핵심 특징

-   **실시간 통신**: WebSocket(STOMP) 기반 1:1 채팅
-   **소셜 기능**: 게시글 작성, 댓글, 좋아요, 알림
-   **이미지 처리**: 게시글 및 메시지 이미지 업로드/표시
-   **반응형 디자인**: X(Twitter) 스타일의 모던한 UI
-   **세션 관리**: HttpSession 기반 사용자 인증

---

##  주요 기능

### 1. 사용자 인증 및 프로필 관리
- 로그인/로그아웃
- 프로필 이미지 업로드 및 수정
- 마이페이지에서 본인 게시글 조회

### 2. 게시글 기능
- 게시글 작성 (텍스트 + 이미지)
- 게시글 조회 (전체/개별/사용자별)
- 게시글 삭제 (Soft Delete)
- 게시글 상세 모달 뷰

### 3. 소셜 상호작용
- 좋아요 (토글 방식)
- 댓글 작성 및 조회
- 알림 시스템 (좋아요/댓글 알림)

### 4. 실시간 채팅
- WebSocket 기반 1:1 채팅
- 대화 목록 조회 (최신 메시지 기준 정렬)
- 채팅 내역 조회
- 이미지 전송 기능
- 실시간 메시지 수신

### 5. 알림 시스템
- 좋아요 알림
- 댓글 알림
- 알림 목록 조회
- 알림 클릭 시 해당 게시글 모달 표시

---

##  기술 스택

### Backend
- **Framework**: Spring Boot 3.3.5
- **Language**: Java 19
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA / Hibernate
- **WebSocket**: Spring WebSocket (STOMP)
- **Template Engine**: Thymeleaf
- **Build Tool**: Maven

### Frontend
- **HTML5 / CSS3**
- **JavaScript (Vanilla)**
- **SockJS / STOMP.js** (WebSocket 클라이언트)

### 주요 라이브러리
- **Lombok**: Boilerplate 코드 감소
- **Commons IO**: 파일 처리
- **JSON**: JSON 파싱

---

##  프로젝트 구조

```
inhatc/
├── src/main/java/kr/co/inhatc/inhatc/
│   ├── controller/          # REST API 및 페이지 컨트롤러
│   │   ├── PostController.java
│   │   ├── CommentController.java
│   │   ├── MemberController.java
│   │   ├── ChatController.java
│   │   ├── MessageController.java
│   │   ├── NotificationController.java
│   │   └── ...
│   ├── service/             # 비즈니스 로직
│   │   ├── PostService.java
│   │   ├── CommentService.java
│   │   ├── MemberService.java
│   │   ├── MessageService.java
│   │   └── NotificationService.java
│   ├── repository/          # JPA Repository
│   │   ├── PostRepository.java
│   │   ├── CommentRepository.java
│   │   ├── MemberRepository.java
│   │   └── MessageRepository.java
│   ├── entity/              # JPA 엔티티
│   │   ├── PostEntity.java
│   │   ├── MemberEntity.java
│   │   ├── CommentEntity.java
│   │   ├── MessageEntity.java
│   │   └── NotificationEntity.java
│   ├── dto/                 # 데이터 전송 객체
│   │   ├── PostRequestDTO.java
│   │   ├── PostResponseDTO.java
│   │   ├── MemberDTO.java
│   │   └── MessageDTO.java
│   ├── WebSocketConfig.java # WebSocket 설정
│   ├── ChatWebSocketHandler.java # WebSocket 메시지 핸들러
│   └── exception/           # 예외 처리
│       └── GlobalExceptionHandler.java
├── src/main/resources/
│   ├── templates/           # Thymeleaf 템플릿
│   │   ├── main.html        # 메인 피드 페이지
│   │   ├── mypage.html      # 마이페이지
│   │   ├── message.html     # 채팅 페이지
│   │   ├── login.html       # 로그인 페이지
│   │   └── notification.html
│   ├── static/              # 정적 리소스
│   │   ├── images/          # 아이콘 이미지
│   │   └── ...
│   └── application.properties
└── pom.xml
```

---

## 🗄 데이터베이스 설계

### 주요 엔티티

#### 1. MemberEntity (회원)
- `id`: 회원 ID (PK)
- `member_email`: 이메일 (UK)
- `member_password`: 비밀번호
- `member_name`: 이름
- `profile_picture_path`: 프로필 이미지 경로

#### 2. PostEntity (게시글)
- `id`: 게시글 ID (PK)
- `content`: 게시글 내용
- `imgsource`: 이미지 경로
- `member_email`: 작성자 이메일
- `love`: 좋아요 수
- `hits`: 조회수
- `delete_yn`: 삭제 여부 (Y/N)
- `created_date`: 작성일시
- `modified_date`: 수정일시

#### 3. CommentEntity (댓글)
- `id`: 댓글 ID (PK)
- `comment`: 댓글 내용
- `post_id`: 게시글 ID (FK)
- `member_email`: 작성자 이메일
- `create_date`: 작성일시

#### 4. LikeEntity (좋아요)
- `post_id`: 게시글 ID (FK)
- `member_email`: 좋아요한 사용자 이메일
- 복합 키로 중복 방지

#### 5. MessageEntity (메시지)
- `id`: 메시지 ID (PK)
- `sender`: 발신자 이메일
- `receiver`: 수신자 이메일
- `content`: 메시지 내용
- `image_path`: 이미지 경로
- `timestamp`: 전송 시간

#### 6. NotificationEntity (알림)
- `id`: 알림 ID (PK)
- `post_id`: 게시글 ID (FK)
- `notification_type`: 알림 타입 (LIKE/COMMENT)
- `actor_email`: 행위자 이메일
- `recipient_email`: 수신자 이메일
- `is_read`: 읽음 여부
- `created_at`: 생성일시

---

##  실행 방법

### 사전 요구사항
- Java 19 이상
- Maven 3.6 이상
- MySQL 8.0 이상

### 1. 데이터베이스 설정

MySQL에서 데이터베이스 생성:
```sql
CREATE DATABASE member CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. application.properties 설정

`src/main/resources/application.properties` 파일을 확인하고 데이터베이스 연결 정보를 수정하세요:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/member?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
```

### 3. 프로젝트 실행

```bash
# 프로젝트 디렉토리로 이동
cd inhatc

# Maven 빌드
mvn clean install

# 애플리케이션 실행
mvn spring-boot:run
```

또는 IDE에서 `InhatcApplication.java`를 실행하세요.

### 4. 접속

브라우저에서 `http://localhost:8080` 접속

---

##  API 문서

### 게시글 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/posts` | 전체 게시글 조회 |
| GET | `/api/posts/{id}` | 게시글 상세 조회 |
| GET | `/api/posts/user/name/{memberName}` | 사용자별 게시글 조회 |
| POST | `/api/posts` | 게시글 작성 |
| DELETE | `/api/posts/{id}` | 게시글 삭제 |
| POST | `/api/posts/{postId}/likes` | 좋아요 토글 |

### 댓글 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/posts/{postId}/comments` | 댓글 목록 조회 |
| POST | `/api/posts/{postId}/comments` | 댓글 작성 |
| DELETE | `/api/comments/{commentId}` | 댓글 삭제 |

### 메시지 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/messages/history` | 채팅 내역 조회 |
| GET | `/api/messages/conversations` | 대화 목록 조회 |
| POST | `/api/messages/upload-image` | 메시지 이미지 업로드 |

### 알림 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/posts/notifications` | 알림 목록 조회 (JSON) |
| GET | `/posts/check` | 알림 페이지 (HTML) |

### WebSocket

- **엔드포인트**: `/ws`
- **메시지 전송**: `/app/chat.sendMessage`
- **구독 경로**: `/topic/chat/{roomName}`

---

##  주요 화면

### 1. 메인 피드 (`/main`)
- 게시글 작성 폼
- 전체 게시글 피드
- 좋아요/댓글 기능
- 알림 아이콘

### 2. 마이페이지 (`/api/members/mypage`)
- 프로필 정보
- 본인 게시글 목록
- 프로필 이미지 수정

### 3. 채팅 페이지 (`/chat/message`)
- 대화 목록 모드
- 1:1 채팅 화면
- 실시간 메시지 수신
- 이미지 전송

### 4. 알림 페이지 (`/posts/check`)
- 좋아요/댓글 알림 목록
- 알림 클릭 시 해당 게시글 모달 표시

---

##  주요 구현 사항

### 1. WebSocket 실시간 채팅
- **STOMP 프로토콜** 사용
- **SockJS** Fallback 지원
- 방(Room) 기반 메시지 라우팅
- 메시지 영구 저장 (DB)

### 2. 이미지 업로드
- 사용자별 폴더 구조로 관리
- 프로필 이미지: `{email}/profile.png`
- 게시글 이미지: `{email}/{timestamp}.{ext}`
- 메시지 이미지: `{email}/messages/{timestamp}.{ext}`

### 3. 알림 시스템
- 좋아요/댓글 시 자동 알림 생성
- 읽음/안읽음 상태 관리
- 실시간 알림 카운트 (폴링 방식)

### 4. 세션 관리
- `HttpSession` 기반 인증
- 로그인 상태 확인 미들웨어
- 세션 만료 시 로그인 페이지 리다이렉트

---

## 개발 이슈 및 해결

### 1. 순환 참조 문제
- **문제**: `PostService`와 `NotificationService` 간 순환 참조
- **해결**: `@Lazy` 어노테이션으로 지연 초기화

### 2. Thymeleaf URL 인코딩
- **문제**: 동적 URL 파라미터에서 특수문자 오류
- **해결**: `th:href` 사용으로 서버 사이드 URL 생성

### 3. WebSocket 연결 안정성
- **문제**: 네트워크 불안정 시 연결 끊김
- **해결**: SockJS Fallback 및 재연결 로직 구현

---

## 학습 내용

- Spring Boot 3.x 기반 RESTful API 설계
- JPA를 활용한 데이터베이스 연동
- WebSocket을 통한 실시간 통신 구현
- Thymeleaf 템플릿 엔진 활용
- 세션 기반 인증 및 권한 관리
- 파일 업로드/다운로드 처리
- 예외 처리 및 글로벌 핸들러 구현

---


