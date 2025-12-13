# Docker 로그 에러 해결 방안

## 🔍 발견된 에러 분석

### 1. ClientAbortException: Broken pipe
**에러**: `java.io.IOException: Broken pipe`
**원인**: 클라이언트가 응답을 받기 전에 연결을 끊음
**심각도**: ⚠️ 경고 (정상적인 상황일 수 있음)

### 2. HttpMessageNotWritableException
**에러**: `No converter for [class ErrorResponse] with preset Content-Type 'image/png'`
**원인**: 이미지 요청(`image/png`)에 대한 에러 응답을 JSON으로 변환하려다 실패
**심각도**: 🔴 중요 (수정 필요)

### 3. 정적 리소스 없음
**에러**: `정적 리소스를 찾을 수 없음: test@naver.com/profile.png`
**원인**: 프로필 이미지 파일이 존재하지 않음
**심각도**: ⚠️ 경고 (정상적인 상황일 수 있음)

### 4. Hibernate 쿼리 경고
**에러**: `HHH90003004: firstResult/maxResults specified with collection fetch; applying in memory`
**원인**: Collection fetch와 페이징을 함께 사용하여 메모리에서 처리
**심각도**: ⚠️ 경고 (성능 최적화 필요)

---

## ✅ 해결 방안

### 문제 1: HttpMessageNotWritableException 해결

**문제**: 이미지 요청에 대한 에러 응답 처리 실패

**해결 방법**: `GlobalExceptionHandler`에서 이미지 요청에 대한 에러 응답을 적절히 처리

#### 수정 사항

```java
@ExceptionHandler(IOException.class)
public ResponseEntity<?> handleIOException(IOException ex, HttpServletRequest request) {
    String requestPath = request.getRequestURI();
    String contentType = request.getHeader("Accept");
    
    // 이미지 요청인 경우
    if (requestPath.endsWith(".png") || requestPath.endsWith(".jpg") || 
        requestPath.endsWith(".jpeg") || requestPath.endsWith(".gif") ||
        (contentType != null && contentType.contains("image/"))) {
        
        // 404 이미지 반환 또는 빈 응답
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.IMAGE_PNG)
            .body(new byte[0]); // 빈 바이트 배열 또는 기본 이미지
    }
    
    // 일반 요청인 경우 JSON 에러 응답
    ErrorResponse errorResponse = ErrorResponse.builder()
        .message("정적 리소스를 찾을 수 없음: " + requestPath)
        .timestamp(LocalDateTime.now())
        .build();
    
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(errorResponse);
}
```

### 문제 2: 정적 리소스 없음 경고 처리

**문제**: 프로필 이미지가 없을 때 경고 로그 발생

**해결 방법**: 
1. 기본 프로필 이미지 제공
2. 에러 로그 레벨 조정 (WARN → DEBUG)

#### 수정 사항

```java
@ExceptionHandler(IOException.class)
public ResponseEntity<?> handleIOException(IOException ex, HttpServletRequest request) {
    String requestPath = request.getRequestURI();
    
    // 정적 리소스 요청인 경우
    if (requestPath.contains("/static/") || requestPath.contains("/images/")) {
        // DEBUG 레벨로 변경하거나 기본 이미지 반환
        log.debug("정적 리소스를 찾을 수 없음: {}", requestPath);
        
        // 기본 이미지 반환
        try {
            Resource defaultImage = new ClassPathResource("static/images/default-profile.png");
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(Files.readAllBytes(defaultImage.getFile().toPath()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    // 일반 에러 처리
    ErrorResponse errorResponse = ErrorResponse.builder()
        .message(ex.getMessage())
        .timestamp(LocalDateTime.now())
        .build();
    
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(errorResponse);
}
```

### 문제 3: Hibernate 쿼리 최적화

**문제**: Collection fetch와 페이징을 함께 사용하여 메모리에서 처리

**해결 방법**: 쿼리 최적화

#### 수정 사항

```java
// ❌ 문제가 있는 쿼리
@Query("SELECT p FROM PostEntity p JOIN FETCH p.comments WHERE p.memberEmail = :email")
List<PostEntity> findByMemberEmailWithComments(@Param("email") String email, Pageable pageable);

// ✅ 수정된 쿼리 (방법 1: BatchSize 사용)
@Query("SELECT p FROM PostEntity p WHERE p.memberEmail = :email")
@BatchSize(size = 10)
List<PostEntity> findByMemberEmail(@Param("email") String email, Pageable pageable);

// ✅ 수정된 쿼리 (방법 2: 별도 쿼리로 분리)
@Query("SELECT p FROM PostEntity p WHERE p.memberEmail = :email")
List<PostEntity> findByMemberEmail(@Param("email") String email, Pageable pageable);

@Query("SELECT c FROM CommentEntity c WHERE c.post.id IN :postIds")
List<CommentEntity> findCommentsByPostIds(@Param("postIds") List<Long> postIds);
```

### 문제 4: Broken Pipe 에러 처리

**문제**: 클라이언트가 연결을 끊었을 때 에러 로그 발생

**해결 방법**: Broken Pipe 에러는 정상적인 상황이므로 로그 레벨 조정

#### 수정 사항

```java
@ExceptionHandler(ClientAbortException.class)
public void handleClientAbortException(ClientAbortException ex) {
    // Broken Pipe는 클라이언트가 연결을 끊은 정상적인 상황
    // DEBUG 레벨로 로깅
    log.debug("Client aborted connection: {}", ex.getMessage());
}

// 또는 application.properties에서 로그 레벨 조정
logging.level.org.apache.catalina.connector.ClientAbortException=DEBUG
```

---

## 🔧 즉시 적용 가능한 해결책

### 1. application.properties 수정

```properties
# 로그 레벨 조정
logging.level.org.apache.catalina.connector.ClientAbortException=DEBUG
logging.level.org.hibernate.orm.query=WARN
logging.level.kr.co.inhatc.inhatc.exception.GlobalExceptionHandler=INFO

# Hibernate 최적화
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
```

### 2. GlobalExceptionHandler 수정

```java
@ExceptionHandler(IOException.class)
public ResponseEntity<?> handleIOException(IOException ex, HttpServletRequest request) {
    String requestPath = request.getRequestURI();
    String acceptHeader = request.getHeader("Accept");
    
    // 이미지 요청인 경우
    if (isImageRequest(requestPath, acceptHeader)) {
        log.debug("이미지 리소스를 찾을 수 없음: {}", requestPath);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.IMAGE_PNG)
            .body(new byte[0]);
    }
    
    // 일반 요청인 경우
    log.warn("정적 리소스를 찾을 수 없음: {}", requestPath);
    ErrorResponse errorResponse = ErrorResponse.builder()
        .message("리소스를 찾을 수 없습니다: " + requestPath)
        .timestamp(LocalDateTime.now())
        .build();
    
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(errorResponse);
}

private boolean isImageRequest(String path, String acceptHeader) {
    return path.endsWith(".png") || path.endsWith(".jpg") || 
           path.endsWith(".jpeg") || path.endsWith(".gif") ||
           (acceptHeader != null && acceptHeader.contains("image/"));
}
```

### 3. 기본 프로필 이미지 추가

```java
@GetMapping("/{email}/profile.png")
public ResponseEntity<byte[]> getProfileImage(@PathVariable String email) {
    try {
        Path imagePath = Paths.get("uploads", email, "profile.png");
        if (Files.exists(imagePath)) {
            byte[] imageBytes = Files.readAllBytes(imagePath);
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(imageBytes);
        } else {
            // 기본 이미지 반환
            Resource defaultImage = new ClassPathResource("static/images/default-profile.png");
            byte[] defaultBytes = Files.readAllBytes(defaultImage.getFile().toPath());
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(defaultBytes);
        }
    } catch (Exception e) {
        log.debug("프로필 이미지를 찾을 수 없음: {}", email);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
```

---

## 📋 우선순위별 해결 방안

### 🔴 높은 우선순위 (즉시 수정)

1. **HttpMessageNotWritableException**
   - GlobalExceptionHandler에서 이미지 요청 처리 개선
   - Content-Type에 따라 적절한 응답 반환

### ⚠️ 중간 우선순위 (성능 개선)

2. **Hibernate 쿼리 최적화**
   - Collection fetch와 페이징 분리
   - BatchSize 사용

### 💡 낮은 우선순위 (로깅 개선)

3. **Broken Pipe 에러**
   - 로그 레벨을 DEBUG로 조정
   - 정상적인 상황이므로 무시 가능

4. **정적 리소스 없음 경고**
   - 기본 이미지 제공
   - 로그 레벨 조정

---

## 🚀 빠른 해결 방법

### 방법 1: 로그 레벨만 조정 (가장 빠름)

`application.properties`에 추가:

```properties
# 에러 로그 레벨 조정
logging.level.org.apache.catalina.connector.ClientAbortException=DEBUG
logging.level.kr.co.inhatc.inhatc.exception.GlobalExceptionHandler=INFO
logging.level.org.hibernate.orm.query=WARN
```

### 방법 2: GlobalExceptionHandler 수정 (권장)

이미지 요청에 대한 에러 응답을 적절히 처리하도록 수정

### 방법 3: 기본 이미지 제공

프로필 이미지가 없을 때 기본 이미지를 반환

---

**작성일**: 2025-12-13  
**우선순위**: HttpMessageNotWritableException > Hibernate 최적화 > 로깅 개선

