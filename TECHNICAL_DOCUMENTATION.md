# 🔧 기술 문서 및 소스코드 분석

면접 대비를 위한 프로젝트의 기술적 구현 사항과 소스코드 분석 문서입니다.

---

## 📑 목차

1. [아키텍처 패턴 및 설계 원칙](#1-아키텍처-패턴-및-설계-원칙)
2. [레이어드 아키텍처 상세 분석](#2-레이어드-아키텍처-상세-분석)
3. [주요 클래스 상세 분석](#3-주요-클래스-상세-분석)
4. [WebSocket 실시간 통신 구현](#4-websocket-실시간-통신-구현)
5. [데이터베이스 설계 및 JPA 관계 매핑](#5-데이터베이스-설계-및-jpa-관계-매핑)
6. [트랜잭션 관리](#6-트랜잭션-관리)
7. [예외 처리 전략](#7-예외-처리-전략)
8. [성능 최적화 포인트](#8-성능-최적화-포인트)
9. [보안 고려사항](#9-보안-고려사항)
10. [면접 예상 질문과 답변](#10-면접-예상-질문과-답변)

---

## 1. 아키텍처 패턴 및 설계 원칙

### 1.1 레이어드 아키텍처 (Layered Architecture)

프로젝트는 **3-Tier Architecture**를 따릅니다:

```
┌─────────────────────────────────────┐
│   Presentation Layer (Controller)   │
│   - PostController                  │
│   - MemberController                │
│   - ChatController                  │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│   Business Layer (Service)          │
│   - PostService                     │
│   - MemberService                   │
│   - MessageService                  │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│   Data Access Layer (Repository)    │
│   - PostRepository                  │
│   - MemberRepository                │
│   - MessageRepository               │
└─────────────────────────────────────┘
```

**장점:**
- 관심사의 분리 (Separation of Concerns)
- 유지보수성 향상
- 테스트 용이성
- 재사용성 증대

### 1.2 DTO 패턴

**Entity와 DTO 분리 이유:**
- **Entity**: 데이터베이스 스키마와 직접 매핑, JPA 영속성 관리
- **DTO**: API 응답/요청 전용 객체, 비즈니스 로직과 분리

**예시: `PostResponseDTO`**
```java
// Entity에서 DTO로 변환
public static PostResponseDTO fromEntity(PostEntity post, MemberEntity member) {
    return new PostResponseDTO(post, member);
}
```

**이점:**
- API 계약 변경 시 Entity에 영향 없음
- 불필요한 데이터 노출 방지
- 순환 참조 방지

### 1.3 의존성 주입 (Dependency Injection)

**생성자 주입 방식 사용:**
```java
@Service
public class PostService {
    private final PostRepository postRepository;
    private final NotificationService notificationService;
    
    // 생성자 주입
    public PostService(PostRepository postRepository,
                       @Lazy NotificationService notificationService) {
        this.postRepository = postRepository;
        this.notificationService = notificationService;
    }
}
```

**장점:**
- 불변성 보장 (`final` 키워드)
- 순환 참조 컴파일 타임 감지
- 테스트 시 Mock 주입 용이

---

## 2. 레이어드 아키텍처 상세 분석

### 2.1 Controller Layer

**역할:**
- HTTP 요청/응답 처리
- 요청 파라미터 검증
- 세션 관리
- Service Layer 호출

**예시: `PostController`**
```java
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    
    @GetMapping
    public ResponseEntity<List<PostResponseDTO>> getAllPosts() {
        return ResponseEntity.ok(postService.findAll());
    }
    
    @PostMapping("/{postId}/likes")
    public ResponseEntity<Map<String, Object>> toggleLove(
            @PathVariable Long postId,
            @RequestParam String email) {
        boolean liked = postService.toggleLove(postId, email);
        // ...
    }
}
```

**설계 원칙:**
- **RESTful API 설계**: 리소스 중심 URL 구조
- **HTTP 상태 코드 활용**: `ResponseEntity`로 명확한 응답
- **단일 책임 원칙**: 각 메서드는 하나의 기능만 수행

### 2.2 Service Layer

**역할:**
- 비즈니스 로직 구현
- 트랜잭션 관리
- 여러 Repository 조합
- 예외 처리

**예시: `PostService.toggleLove()`**
```java
@Transactional
public boolean toggleLove(Long postId, String email) {
    // 1. 엔티티 조회
    PostEntity post = postRepository.findById(postId)
            .orElseThrow(() -> new CustomException(ErrorCode.POSTS_NOT_FOUND));
    
    // 2. 비즈니스 로직 (좋아요 토글)
    Optional<LikeEntity> existingLike = likeRepository.findByPostAndUser(post, user);
    if (existingLike.isPresent()) {
        likeRepository.delete(existingLike.get());
        post.decreaseLove();
    } else {
        LikeEntity newLike = new LikeEntity(post, user);
        likeRepository.save(newLike);
        post.increaseLove();
        
        // 3. 알림 생성 (비동기 처리 가능)
        notificationService.createLikeNotification(postId, email);
    }
    return existingLike.isEmpty();
}
```

**핵심 포인트:**
- **트랜잭션 경계**: `@Transactional`로 일관성 보장
- **도메인 로직 캡슐화**: Entity의 `increaseLove()`, `decreaseLove()` 메서드 활용
- **예외 처리**: `CustomException`으로 명확한 에러 메시지

### 2.3 Repository Layer

**역할:**
- 데이터베이스 접근
- JPA 쿼리 메서드
- 커스텀 쿼리 작성

**예시: `PostRepository`**
```java
@Repository
public interface PostRepository extends JpaRepository<PostEntity, Long> {
    // 메서드 이름 기반 쿼리 생성
    List<PostEntity> findByDeleteYnOrderByCreatedDateDesc(char deleteYn);
    
    // 커스텀 JPQL 쿼리
    @Query("SELECT p FROM PostEntity p WHERE p.memberEmail IN :emails ORDER BY p.id DESC")
    List<PostEntity> findByMemberEmails(@Param("emails") List<String> emails);
}
```

**최적화 포인트:**
- **메서드 이름 기반 쿼리**: 간단한 쿼리는 자동 생성 활용
- **JPQL 커스텀 쿼리**: 복잡한 쿼리는 명시적 작성
- **Fetch 전략**: `@ManyToOne(fetch = FetchType.LAZY)`로 N+1 문제 방지

---

## 3. 주요 클래스 상세 분석

### 3.1 PostService - 게시글 비즈니스 로직

#### 3.1.1 순환 참조 해결

**문제 상황:**
```
PostService → NotificationService
NotificationService → PostService (순환 참조 발생)
```

**해결 방법: `@Lazy` 어노테이션**
```java
public PostService(PostRepository postRepository,
                   @Lazy NotificationService notificationService) {
    // @Lazy로 지연 초기화하여 순환 참조 방지
}
```

**면접 질문 대비:**
- **Q: 순환 참조를 어떻게 해결했나요?**
- **A: `@Lazy` 어노테이션을 사용하여 NotificationService를 프록시 객체로 지연 초기화했습니다. 이렇게 하면 PostService 생성 시점에는 실제 NotificationService 인스턴스가 아닌 프록시가 주입되어 순환 참조를 방지할 수 있습니다.**

#### 3.1.2 Soft Delete 구현

```java
@Transactional
public void delete(Long postId) {
    PostEntity post = postRepository.findById(postId)
            .orElseThrow(() -> new CustomException(ErrorCode.POSTS_NOT_FOUND));
    
    // 실제 삭제 대신 플래그 설정 (Soft Delete)
    post.setDeleteYn('Y');
    postRepository.save(post);
}
```

**이유:**
- 데이터 복구 가능
- 관련 데이터 무결성 유지 (댓글, 좋아요 등)
- 감사(Audit) 목적

#### 3.1.3 이미지 업로드 처리

```java
public String imgupload(MultipartFile file, String email) throws IOException {
    // 1. 사용자별 디렉토리 생성
    File userDir = new File(baseDir, email);
    if (!userDir.exists()) {
        userDir.mkdirs();
    }
    
    // 2. 고유한 파일명 생성 (타임스탬프 기반)
    String newFilename = new SimpleDateFormat("yyyyMMddHHmmssSSS")
            .format(new Date()) + "." + extension;
    
    // 3. 파일 저장
    File dest = new File(userDir, newFilename);
    file.transferTo(dest);
    
    // 4. DB에 저장할 URL 경로 반환
    return "/posts/" + email + "/" + newFilename;
}
```

**보안 고려사항:**
- 파일명 중복 방지 (타임스탬프 사용)
- 사용자별 디렉토리 분리
- 확장자 검증 필요 (현재 미구현, 개선 가능)

### 3.2 CommentService - 댓글 비즈니스 로직

#### 3.2.1 권한 검증

```java
public void updateComment(Long postId, Long commentId, CommentRequestDTO requestDTO) {
    CommentEntity comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("댓글이 존재하지 않습니다."));
    
    // 1. 게시글 일치 여부 확인
    if (!comment.getPost().getId().equals(postId)) {
        throw new RuntimeException("해당 댓글은 이 게시글에 속하지 않습니다.");
    }
    
    // 2. 작성자 일치 여부 확인 (보안)
    if (!comment.getWriter().getMemberEmail().equals(requestDTO.getUser())) {
        throw new RuntimeException("본인이 작성한 댓글만 수정할 수 있습니다.");
    }
    
    comment.update(requestDTO.getComment());
    commentRepository.save(comment);
}
```

**보안 포인트:**
- **수평 권한 검증**: 본인 데이터만 수정 가능
- **수직 권한 검증**: 관리자 권한 분리 가능 (현재 미구현)

### 3.3 MessageService - 메시지 비즈니스 로직

#### 3.3.1 대화 목록 조회 최적화

```java
public List<ConversationDTO> getConversations(String userEmail) {
    // 1. 사용자와 관련된 모든 메시지 조회
    List<MessageEntity> messages = messageRepository
            .findConversationsByUserEmail(userEmail);
    
    // 2. 상대방별로 그룹화하고 최신 메시지만 유지
    Map<String, MessageEntity> conversationMap = new LinkedHashMap<>();
    
    for (MessageEntity message : messages) {
        String otherUserEmail = message.getSender().equals(userEmail) 
            ? message.getReceiver() 
            : message.getSender();
        
        // 이미 존재하지 않거나 더 최신 메시지인 경우 업데이트
        if (!conversationMap.containsKey(otherUserEmail) ||
            message.getTimestamp().isAfter(conversationMap.get(otherUserEmail).getTimestamp())) {
            conversationMap.put(otherUserEmail, message);
        }
    }
    
    // 3. 최신 메시지 시간 순으로 정렬
    conversations.sort((a, b) -> 
        b.getLastMessageTime().compareTo(a.getLastMessageTime()));
    
    return conversations;
}
```

**최적화 포인트:**
- **메모리에서 그룹화**: DB 쿼리 1회로 최소화
- **LinkedHashMap 사용**: 삽입 순서 유지
- **정렬 최적화**: 최신 메시지 기준 정렬

**개선 가능 사항:**
- 페이징 처리 (대화가 많을 경우)
- 읽지 않은 메시지 수 집계 최적화

---

## 4. WebSocket 실시간 통신 구현

### 4.1 WebSocket 설정 (`WebSocketConfig`)

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // CORS 설정
                .withSockJS();  // Fallback 지원
    }
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");  // 구독 경로
        config.setApplicationDestinationPrefixes("/app");  // 메시지 전송 경로
    }
}
```

**설명:**
- **STOMP 프로토콜**: 텍스트 기반 메시징 프로토콜
- **SockJS**: WebSocket 미지원 브라우저를 위한 Fallback
- **Simple Broker**: 인메모리 메시지 브로커 (RabbitMQ, Redis로 확장 가능)

### 4.2 메시지 핸들러 (`ChatWebSocketHandler`)

```java
@Controller
@RequiredArgsConstructor
public class ChatWebSocketHandler {
    
    private final MessageService messageService;
    
    @MessageMapping("/chat/{roomName}/sendMessage")
    @SendTo("/topic/{roomName}")
    public ObjectNode sendMessageToRoom(@Payload ObjectNode message, String roomName) {
        // 1. 메시지 파싱
        String sender = message.get("sender").asText();
        String receiver = message.get("receiver").asText();
        String content = message.has("content") ? message.get("content").asText() : "";
        String imagePath = message.has("imagePath") ? message.get("imagePath").asText() : null;
        
        // 2. DB에 저장
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setSender(sender);
        messageDTO.setReceiver(receiver);
        messageDTO.setContent(content);
        messageDTO.setImagePath(imagePath);
        messageDTO.setTimestamp(LocalDateTime.now());
        messageService.saveMessage(messageDTO);
        
        // 3. 구독자에게 브로드캐스트
        ObjectNode response = objectMapper.createObjectNode();
        response.put("sender", sender);
        response.put("receiver", receiver);
        response.put("content", content);
        if (imagePath != null) {
            response.put("imagePath", imagePath);
        }
        response.put("timestamp", LocalDateTime.now().toString());
        
        return response;  // @SendTo로 자동 브로드캐스트
    }
}
```

**동작 흐름:**
1. 클라이언트가 `/app/chat/{roomName}/sendMessage`로 메시지 전송
2. 핸들러에서 메시지 파싱 및 DB 저장
3. `@SendTo`로 `/topic/{roomName}` 구독자에게 브로드캐스트
4. 해당 방의 모든 클라이언트가 실시간으로 메시지 수신

### 4.3 클라이언트 연결 (JavaScript)

```javascript
// WebSocket 연결
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    // 구독
    const roomName = getNormalizedRoomName(senderEmail, receiverEmail);
    stompClient.subscribe('/topic/chat/' + roomName, function(message) {
        const data = JSON.parse(message.body);
        displayMessage(data);
    });
});

// 메시지 전송
function sendMessage() {
    const message = {
        sender: senderEmail,
        receiver: receiverEmail,
        content: messageInput.value,
        imagePath: uploadedImagePath
    };
    
    stompClient.send('/app/chat/' + roomName + '/sendMessage', {}, 
                    JSON.stringify(message));
}
```

**면접 질문 대비:**
- **Q: WebSocket과 HTTP 폴링의 차이점은?**
- **A: WebSocket은 양방향 통신으로 서버가 클라이언트에게 직접 푸시할 수 있어 실시간성이 뛰어납니다. HTTP 폴링은 클라이언트가 주기적으로 요청해야 하므로 지연이 발생하고 서버 부하가 큽니다. 채팅 같은 실시간 기능에는 WebSocket이 적합합니다.**

- **Q: STOMP를 사용한 이유는?**
- **A: STOMP는 텍스트 기반 프로토콜로 메시지 형식이 명확하고, 구독/발행 패턴을 쉽게 구현할 수 있습니다. 또한 Spring에서 지원하는 메시징 추상화를 활용할 수 있어 개발 생산성이 높습니다.**

---

## 5. 데이터베이스 설계 및 JPA 관계 매핑

### 5.1 엔티티 관계도

```
MemberEntity (1) ────< (N) PostEntity
                         │
                         │ (1)
                         │
                         └───< (N) CommentEntity
                         │
                         │ (N)
                         │
                         └───< (N) LikeEntity
                         │
                         │ (1)
                         │
                         └───< (N) NotificationEntity
```

### 5.2 주요 관계 매핑

#### 5.2.1 `@ManyToOne` - 다대일 관계

**PostEntity ↔ CommentEntity**
```java
@Entity
public class CommentEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private PostEntity post;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_email", referencedColumnName = "member_email",
                insertable = false, updatable = false)
    private MemberEntity writer;
}
```

**설명:**
- **LAZY 로딩**: 필요할 때만 연관 엔티티 조회 (N+1 문제 방지)
- **insertable/updatable = false**: 읽기 전용 관계 (member_email 직접 저장)

#### 5.2.2 `@OneToMany` - 일대다 관계

**PostEntity ↔ CommentEntity**
```java
@Entity
public class PostEntity {
    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, 
               fetch = FetchType.EAGER)
    @OrderBy("id ASC")
    private List<CommentEntity> comments = new ArrayList<>();
}
```

**설명:**
- **mappedBy**: 연관관계의 주인은 CommentEntity의 `post` 필드
- **cascade = REMOVE**: 게시글 삭제 시 댓글도 함께 삭제
- **EAGER 로딩**: 게시글 조회 시 댓글도 함께 조회 (성능 고려 필요)

#### 5.2.3 `@ManyToMany` - 다대다 관계

**PostEntity ↔ MemberEntity (좋아요)**
```java
@Entity
public class PostEntity {
    @ManyToMany
    @JoinTable(
        name = "post_likes",
        joinColumns = @JoinColumn(name = "post_id"),
        inverseJoinColumns = @JoinColumn(name = "member_email", 
                                        referencedColumnName = "member_email")
    )
    private Set<MemberEntity> lovedBy = new HashSet<>();
}
```

**설명:**
- **중간 테이블**: `post_likes` 테이블 자동 생성
- **Set 사용**: 중복 방지

**개선 가능 사항:**
- 현재는 `LikeEntity`를 별도로 사용하므로 `@ManyToMany`와 중복
- 일관성을 위해 하나의 방식으로 통일 권장

### 5.3 쿼리 최적화

#### 5.3.1 N+1 문제 해결

**문제 상황:**
```java
// 게시글 목록 조회
List<PostEntity> posts = postRepository.findAll();

// 각 게시글의 댓글 조회 시 N번의 쿼리 발생
for (PostEntity post : posts) {
    post.getComments();  // 각각 쿼리 실행
}
```

**해결 방법 1: Fetch Join**
```java
@Query("SELECT p FROM PostEntity p " +
       "LEFT JOIN FETCH p.comments " +
       "WHERE p.deleteYn = 'N'")
List<PostEntity> findAllWithComments();
```

**해결 방법 2: EntityGraph**
```java
@EntityGraph(attributePaths = {"comments"})
List<PostEntity> findByDeleteYnOrderByCreatedDateDesc(char deleteYn);
```

**현재 프로젝트:**
- `PostEntity.comments`는 `EAGER`로 설정되어 있으나, Service에서 Stream으로 처리하여 부분적으로 최적화

---

## 6. 트랜잭션 관리

### 6.1 `@Transactional` 사용

**선언적 트랜잭션 관리:**
```java
@Service
@Transactional  // 클래스 레벨 적용
public class CommentService {
    
    @Transactional  // 메서드 레벨 (우선순위 높음)
    public CommentResponseDTO addComment(CommentRequestDTO requestDTO) {
        // 여러 DB 작업이 하나의 트랜잭션으로 묶임
        CommentEntity comment = CommentEntity.builder()...build();
        commentRepository.save(comment);
        notificationService.createCommentNotification(...);
        return new CommentResponseDTO(saved);
    }
}
```

**트랜잭션 전파:**
- **기본값: REQUIRED**: 기존 트랜잭션이 있으면 참여, 없으면 생성
- `notificationService.createCommentNotification()`도 같은 트랜잭션에 참여

### 6.2 트랜잭션 격리 수준

**현재 설정:**
- 기본값 사용 (READ_COMMITTED)
- 동시성 문제가 발생할 경우 명시적 설정 필요

**예시: 좋아요 중복 방지**
```java
@Transactional(isolation = Isolation.SERIALIZABLE)
public boolean toggleLove(Long postId, String email) {
    // 동시에 같은 사용자가 좋아요를 누를 경우 방지
}
```

### 6.3 롤백 처리

**예외 발생 시 자동 롤백:**
```java
@Transactional(rollbackFor = Exception.class)  // 모든 예외에 대해 롤백
public void savePost(String email, String content, String filePath) {
    // RuntimeException 또는 Error 발생 시 자동 롤백
}
```

---

## 7. 예외 처리 전략

### 7.1 글로벌 예외 처리 (`GlobalExceptionHandler`)

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(
            final CustomException e) {
        log.error("handleCustomException: {}", e.getErrorCode());
        return ResponseEntity
                .status(e.getErrorCode().getStatus().value())
                .body(new ErrorResponse(e.getErrorCode()));
    }
    
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(final Exception e) {
        log.error("handleException: {}", e.getMessage());
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus().value())
                .body(new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
```

**장점:**
- **중앙 집중식 예외 처리**: 모든 컨트롤러에서 일관된 응답
- **에러 로깅**: 문제 추적 용이
- **사용자 친화적 메시지**: 기술적 에러를 숨기고 명확한 메시지 제공

### 7.2 커스텀 예외 (`CustomException`)

```java
@Getter
@AllArgsConstructor
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;
}
```

**사용 예시:**
```java
PostEntity post = postRepository.findById(id)
        .orElseThrow(() -> new CustomException(ErrorCode.POSTS_NOT_FOUND));
```

**이점:**
- **타입 안정성**: 컴파일 타임에 에러 코드 확인
- **명확한 에러 메시지**: 각 에러 코드에 대한 메시지 정의

### 7.3 에러 코드 정의 (`ErrorCode`)

```java
@Getter
@AllArgsConstructor
public enum ErrorCode {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    POSTS_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글 정보를 찾을 수 없습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원 정보를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류입니다.");
    
    private final HttpStatus status;
    private final String message;
}
```

**장점:**
- **일관된 에러 응답**: 모든 API에서 동일한 형식
- **유지보수 용이**: 에러 메시지 중앙 관리

---

## 8. 성능 최적화 포인트

### 8.1 현재 최적화 사항

#### 8.1.1 Lazy Loading
```java
@ManyToOne(fetch = FetchType.LAZY)
private MemberEntity writer;
```
- 필요할 때만 연관 엔티티 조회
- N+1 문제 부분 해결

#### 8.1.2 Stream API 활용
```java
return posts.stream()
        .filter(post -> post.getDeleteYn() == 'N')
        .map(post -> PostResponseDTO.fromEntity(post, member))
        .collect(Collectors.toList());
```
- 메모리 효율적 처리
- 함수형 프로그래밍으로 가독성 향상

### 8.2 개선 가능 사항

#### 8.2.1 페이징 처리
**현재:**
```java
public List<PostResponseDTO> findAll() {
    List<PostEntity> posts = postRepository.findByDeleteYnOrderByCreatedDateDesc('N');
    // 모든 게시글을 한 번에 조회
}
```

**개선:**
```java
public Page<PostResponseDTO> findAll(Pageable pageable) {
    Page<PostEntity> posts = postRepository.findByDeleteYnOrderByCreatedDateDesc(
        'N', pageable);
    return posts.map(post -> PostResponseDTO.fromEntity(post, member));
}
```

#### 8.2.2 캐싱 전략
```java
@Cacheable(value = "posts", key = "#id")
public PostResponseDTO findById(Long id) {
    // 자주 조회되는 게시글 캐싱
}
```

#### 8.2.3 배치 처리
```java
@Transactional
public void markAllAsRead(String recipientEmail) {
    // 현재: 개별 업데이트
    notifications.forEach(NotificationEntity::markAsRead);
    notificationRepository.saveAll(notifications);
    
    // 개선: 벌크 업데이트
    notificationRepository.updateIsReadByRecipientEmail(recipientEmail);
}
```

---

## 9. 보안 고려사항

### 9.1 현재 구현된 보안

#### 9.1.1 세션 기반 인증
```java
@PostMapping("/upload")
public ResponseEntity<String> uploadImage(..., HttpSession session) {
    String email = (String) session.getAttribute("loginEmail");
    if (email == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("로그인이 필요합니다.");
    }
    // ...
}
```

#### 9.1.2 권한 검증
```java
// 댓글 수정 시 작성자 확인
if (!comment.getWriter().getMemberEmail().equals(requestDTO.getUser())) {
    throw new RuntimeException("본인이 작성한 댓글만 수정할 수 있습니다.");
}
```

### 9.2 개선 필요 사항

#### 9.2.1 비밀번호 암호화
**현재:**
```java
// 평문 비밀번호 저장 (위험!)
if (memberOpt.get().getMemberPassword().equals(password)) {
    return MemberEntity.toDTO(memberOpt.get());
}
```

**개선:**
```java
@Autowired
private PasswordEncoder passwordEncoder;

public MemberDTO login(String email, String password) {
    MemberEntity member = memberRepository.findByMemberEmail(email)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    
    if (passwordEncoder.matches(password, member.getMemberPassword())) {
        return MemberEntity.toDTO(member);
    }
    throw new CustomException(ErrorCode.INVALID_PASSWORD);
}
```

#### 9.2.2 SQL Injection 방지
- **현재**: JPA 사용으로 자동 방지
- **주의**: Native Query 사용 시 `@Param` 활용 필수

#### 9.2.3 XSS 방지
- Thymeleaf 기본 이스케이프 활용
- 사용자 입력 검증 필요

#### 9.2.4 CSRF 방지
- Spring Security 도입 권장
- 현재는 세션 기반으로 부분적 보호

#### 9.2.5 파일 업로드 보안
```java
// 현재: 확장자 검증 없음
// 개선 필요:
private static final List<String> ALLOWED_EXTENSIONS = 
    Arrays.asList("jpg", "jpeg", "png", "gif");

String extension = getExtension(file.getOriginalFilename());
if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
    throw new IllegalArgumentException("허용되지 않은 파일 형식입니다.");
}
```

---

## 10. 면접 예상 질문과 답변

### 10.1 Spring Boot 관련

**Q1: Spring Boot의 자동 설정(Auto Configuration)이 어떻게 동작하나요?**
```
A: Spring Boot는 @EnableAutoConfiguration 어노테이션을 통해 
클래스패스에 있는 라이브러리를 스캔하고, 조건부로 Bean을 자동 등록합니다.
예를 들어, spring-boot-starter-data-jpa를 의존성에 추가하면
DataSource, EntityManagerFactory 등이 자동으로 설정됩니다.
이 프로젝트에서는 application.properties에 데이터베이스 정보만 설정하면
JPA가 자동으로 동작합니다.
```

**Q2: @Transactional의 동작 원리는?**
```
A: @Transactional은 AOP(관점 지향 프로그래밍)를 기반으로 동작합니다.
Spring이 프록시 객체를 생성하여 트랜잭션 시작/커밋/롤백을 자동으로 처리합니다.
메서드 실행 전에 트랜잭션을 시작하고, 정상 종료 시 커밋, 예외 발생 시 롤백합니다.
이 프로젝트의 PostService.toggleLove() 메서드에서 좋아요 추가/삭제와
알림 생성이 하나의 트랜잭션으로 묶여 일관성을 보장합니다.
```

**Q3: 의존성 주입의 세 가지 방식과 각각의 장단점은?**
```
A: 
1. 생성자 주입 (현재 프로젝트에서 사용)
   - 장점: 불변성 보장, 순환 참조 컴파일 타임 감지, 테스트 용이
   - 단점: 생성자 파라미터가 많아지면 복잡해짐

2. 필드 주입
   - 장점: 코드가 간결
   - 단점: 테스트 어려움, final 사용 불가, 순환 참조 위험

3. Setter 주입
   - 장점: 선택적 의존성 주입 가능
   - 단점: 불변성 보장 불가, 런타임에 NullPointerException 가능
```

### 10.2 JPA 관련

**Q4: JPA의 영속성 컨텍스트란?**
```
A: 영속성 컨텍스트는 엔티티를 영구 저장하는 환경입니다.
EntityManager를 통해 엔티티를 관리하며, 1차 캐시, 변경 감지(Dirty Checking),
지연 로딩 등의 기능을 제공합니다.

예를 들어, PostService.findById()에서:
1. postRepository.findById()로 조회하면 1차 캐시에 저장
2. post.increaseHits()로 조회수를 변경
3. 트랜잭션 커밋 시 변경 감지로 자동 UPDATE 쿼리 실행
```

**Q5: N+1 문제를 어떻게 해결했나요?**
```
A: 
1. Fetch Join 사용: @Query("SELECT p FROM PostEntity p LEFT JOIN FETCH p.comments")
2. EntityGraph 사용: @EntityGraph(attributePaths = {"comments"})
3. Lazy Loading: @ManyToOne(fetch = FetchType.LAZY)로 필요할 때만 조회

현재 프로젝트에서는 주로 Lazy Loading을 사용하고 있으며,
대화 목록 조회(getConversations)에서는 메모리에서 그룹화하여
쿼리 횟수를 최소화했습니다.
```

**Q6: JPA의 CascadeType에 대해 설명해주세요.**
```
A: 
- PERSIST: 부모 저장 시 자식도 저장
- MERGE: 부모 병합 시 자식도 병합
- REMOVE: 부모 삭제 시 자식도 삭제 (현재 프로젝트에서 사용)
- REFRESH: 부모 새로고침 시 자식도 새로고침
- DETACH: 부모 분리 시 자식도 분리
- ALL: 위의 모든 작업 전파

PostEntity에서 @OneToMany(cascade = CascadeType.REMOVE)로 설정하여
게시글 삭제 시 댓글도 함께 삭제되도록 했습니다.
```

### 10.3 WebSocket 관련

**Q7: WebSocket과 HTTP의 차이점은?**
```
A: 
HTTP:
- 요청-응답 모델 (단방향)
- 클라이언트가 요청해야 응답 받음
- 연결 유지하지 않음 (Stateless)

WebSocket:
- 양방향 통신
- 서버가 클라이언트에게 직접 푸시 가능
- 연결 유지 (Stateful)
- 실시간 통신에 적합

이 프로젝트에서는 채팅 기능을 위해 WebSocket을 사용하여
메시지를 실시간으로 주고받을 수 있도록 구현했습니다.
```

**Q8: STOMP를 사용한 이유는?**
```
A: STOMP(Simple Text Oriented Messaging Protocol)는 
텍스트 기반 메시징 프로토콜로, WebSocket 위에서 동작합니다.

장점:
1. 구독/발행 패턴 구현 용이
2. 메시지 형식이 명확 (JSON)
3. Spring의 메시징 추상화 활용 가능
4. 라우팅 기능 (roomName 기반)

현재 프로젝트에서는 /topic/chat/{roomName}으로 
사용자별 채팅방을 구분하여 메시지를 라우팅합니다.
```

### 10.4 트랜잭션 관련

**Q9: 트랜잭션 격리 수준에 대해 설명해주세요.**
```
A: 
1. READ_UNCOMMITTED: 커밋되지 않은 데이터 읽기 가능 (Dirty Read)
2. READ_COMMITTED: 커밋된 데이터만 읽기 (기본값, 현재 프로젝트)
3. REPEATABLE_READ: 같은 트랜잭션에서 같은 데이터 반복 읽기 보장
4. SERIALIZABLE: 가장 엄격, 동시성 최소화

현재 프로젝트에서는 기본값을 사용하고 있으며,
좋아요 중복 방지 등이 필요할 경우 SERIALIZABLE로 설정할 수 있습니다.
```

**Q10: 트랜잭션 전파(Propagation)에 대해 설명해주세요.**
```
A: 
- REQUIRED (기본값): 기존 트랜잭션이 있으면 참여, 없으면 생성
- REQUIRES_NEW: 항상 새로운 트랜잭션 생성
- SUPPORTS: 트랜잭션이 있으면 참여, 없으면 트랜잭션 없이 실행
- MANDATORY: 반드시 트랜잭션이 있어야 함, 없으면 예외
- NOT_SUPPORTED: 트랜잭션 없이 실행
- NEVER: 트랜잭션이 있으면 예외
- NESTED: 중첩 트랜잭션 (Savepoint 활용)

현재 프로젝트에서는 PostService.toggleLove()에서
notificationService.createLikeNotification()을 호출할 때
같은 트랜잭션에 참여하여 일관성을 보장합니다.
```

### 10.5 예외 처리 관련

**Q11: @RestControllerAdvice의 역할은?**
```
A: @RestControllerAdvice는 모든 @RestController에서 발생하는
예외를 중앙에서 처리하는 어노테이션입니다.

현재 프로젝트의 GlobalExceptionHandler에서:
1. CustomException: 커스텀 에러 코드로 응답
2. HttpRequestMethodNotSupportedException: 405 에러 처리
3. Exception: 모든 예외를 500 에러로 처리

이를 통해 모든 API에서 일관된 에러 응답 형식을 제공합니다.
```

**Q12: 예외 처리 전략을 어떻게 설계했나요?**
```
A: 
1. 커스텀 예외 정의: CustomException + ErrorCode enum
2. 글로벌 예외 처리: @RestControllerAdvice로 중앙 집중식 처리
3. 명확한 에러 메시지: 사용자 친화적 메시지 제공
4. 로깅: 문제 추적을 위한 에러 로그 기록

예를 들어, PostService.findById()에서 게시글을 찾을 수 없으면
CustomException(ErrorCode.POSTS_NOT_FOUND)를 던지고,
GlobalExceptionHandler에서 404 상태 코드와 함께
"게시글 정보를 찾을 수 없습니다." 메시지를 반환합니다.
```

### 10.6 성능 최적화 관련

**Q13: 성능 최적화를 위해 어떤 방법을 사용했나요?**
```
A: 
1. Lazy Loading: 필요할 때만 연관 엔티티 조회
2. Stream API: 메모리 효율적 처리
3. 인덱스 활용: JPA 메서드 이름으로 자동 인덱스 활용
4. 메모리 그룹화: 대화 목록 조회 시 DB 쿼리 최소화

개선 가능 사항:
- 페이징 처리 (Pageable)
- 캐싱 (Redis)
- 벌크 업데이트
- Fetch Join 최적화
```

**Q14: 대화 목록 조회를 어떻게 최적화했나요?**
```
A: 
1. 단일 쿼리: findConversationsByUserEmail()로 한 번에 조회
2. 메모리 그룹화: Map을 사용하여 상대방별로 최신 메시지만 유지
3. 정렬 최적화: 최신 메시지 시간 기준으로 정렬

이를 통해 N번의 쿼리 대신 1번의 쿼리로 대화 목록을 조회할 수 있습니다.
```

### 10.7 보안 관련

**Q15: 현재 프로젝트의 보안 취약점과 개선 방안은?**
```
A: 
현재 취약점:
1. 비밀번호 평문 저장
2. 파일 업로드 확장자 검증 없음
3. CSRF 방지 미구현
4. XSS 방지 부족

개선 방안:
1. BCrypt 등으로 비밀번호 암호화
2. 화이트리스트 방식으로 파일 확장자 검증
3. Spring Security 도입
4. 입력값 검증 및 이스케이프 처리
```

---

## 📚 추가 학습 자료

### 추천 도서
- "스프링 부트와 AWS로 혼자 구현하는 웹 서비스" (이동욱)
- "자바 ORM 표준 JPA 프로그래밍" (김영한)
- "Real MySQL" (이성욱)

### 온라인 강의
- 인프런: "스프링 부트 - 핵심 원리와 활용" (김영한)
- 인프런: "실전! 스프링 부트와 JPA 활용1" (김영한)

### 공식 문서
- [Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring Data JPA Documentation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [WebSocket Documentation](https://docs.spring.io/spring-framework/reference/web/websocket.html)

---

**마지막 업데이트**: 2025년 11월

**작성자**: 프로젝트 개발자

