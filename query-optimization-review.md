# 쿼리 최적화 및 에러 해결 검토 보고서

## ✅ 해결 완료된 에러

### 1. HttpMessageNotWritableException ✅
- **상태**: 해결 완료
- **수정 내용**: GlobalExceptionHandler에서 이미지 요청에 대한 적절한 Content-Type 처리
- **결과**: 이미지 요청 시 `image/png`로 응답, 일반 요청 시 `application/json`으로 응답

### 2. ClientAbortException: Broken pipe ✅
- **상태**: 해결 완료
- **수정 내용**: 로그 레벨을 DEBUG로 조정
- **결과**: 정상적인 상황으로 처리되어 로그 노이즈 감소

### 3. 정적 리소스 없음 경고 ✅
- **상태**: 해결 완료
- **수정 내용**: 프로필 이미지 요청은 DEBUG 레벨로 처리
- **결과**: 불필요한 경고 로그 감소

---

## ⚠️ 해결 필요: Hibernate 쿼리 최적화 경고

### 발견된 문제

**에러 메시지**: `HHH90003004: firstResult/maxResults specified with collection fetch; applying in memory`

**원인**: `@EntityGraph`와 `Pageable`을 함께 사용하여 페이징과 Collection fetch가 충돌

**발생 위치**:
1. `PostRepository.findByDeleteYnOrderByCreatedDateDesc(Pageable)` - Line 56
2. `PostRepository.findByMemberEmailOrderByIdDesc(Pageable)` - Line 60

### 문제 코드

```java
// ❌ 문제가 있는 코드
@EntityGraph(attributePaths = {"comments"})
Page<PostEntity> findByDeleteYnOrderByCreatedDateDesc(char deleteYn, Pageable pageable);

@EntityGraph(attributePaths = {"comments"})
Page<PostEntity> findByMemberEmailOrderByIdDesc(String memberEmail, Pageable pageable);
```

**문제점**:
- `@EntityGraph`는 JOIN FETCH와 유사하게 동작
- 페이징과 Collection fetch를 함께 사용하면 메모리에서 처리됨
- 대량 데이터 처리 시 성능 저하 발생

---

## 🔧 해결 방안

### 방안 1: @BatchSize 사용 (권장)

`@EntityGraph`를 제거하고 `@BatchSize`를 사용하여 지연 로딩 최적화

```java
// ✅ 수정된 코드
@BatchSize(size = 20)
Page<PostEntity> findByDeleteYnOrderByCreatedDateDesc(char deleteYn, Pageable pageable);

@BatchSize(size = 20)
Page<PostEntity> findByMemberEmailOrderByIdDesc(String memberEmail, Pageable pageable);
```

**장점**:
- 페이징과 호환됨
- N+1 문제 해결
- 메모리 효율적

**단점**:
- 추가 쿼리 발생 (하지만 배치로 처리되어 효율적)

### 방안 2: 별도 쿼리로 분리

Post는 페이징으로 조회하고, Comments는 별도 쿼리로 조회

```java
// Post 조회 (페이징)
Page<PostEntity> findByDeleteYnOrderByCreatedDateDesc(char deleteYn, Pageable pageable);

// Comments 조회 (별도 쿼리)
@Query("SELECT c FROM CommentEntity c WHERE c.post.id IN :postIds")
List<CommentEntity> findCommentsByPostIds(@Param("postIds") List<Long> postIds);
```

**장점**:
- 페이징 정확히 작동
- 쿼리 최적화 가능

**단점**:
- Service 로직 복잡도 증가

### 방안 3: @EntityGraph 제거 (간단한 해결)

`@EntityGraph`를 제거하고 지연 로딩 사용

```java
// ✅ 수정된 코드
Page<PostEntity> findByDeleteYnOrderByCreatedDateDesc(char deleteYn, Pageable pageable);

Page<PostEntity> findByMemberEmailOrderByIdDesc(String memberEmail, Pageable pageable);
```

**장점**:
- 간단한 수정
- 페이징 정확히 작동

**단점**:
- N+1 문제 발생 가능 (하지만 @BatchSize로 완화 가능)

---

## 📋 권장 해결 방법

### 단계별 적용

#### 1단계: PostEntity에 @BatchSize 추가

```java
@Entity
@BatchSize(size = 20)  // 추가
public class PostEntity {
    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)  // EAGER → LAZY 변경
    @OrderBy("id ASC")
    private List<CommentEntity> comments = new ArrayList<>();
}
```

#### 2단계: PostRepository에서 @EntityGraph 제거

```java
// ✅ 수정된 코드
Page<PostEntity> findByDeleteYnOrderByCreatedDateDesc(char deleteYn, Pageable pageable);

Page<PostEntity> findByMemberEmailOrderByIdDesc(String memberEmail, Pageable pageable);
```

#### 3단계: application.properties에 배치 설정 추가

```properties
# Hibernate 배치 처리 설정
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
```

---

## 🔍 현재 상태 요약

| 에러 | 상태 | 우선순위 |
|------|------|----------|
| HttpMessageNotWritableException | ✅ 해결 완료 | - |
| ClientAbortException | ✅ 해결 완료 | - |
| 정적 리소스 없음 | ✅ 해결 완료 | - |
| Hibernate 쿼리 경고 | ⚠️ 해결 필요 | 중간 |

---

## 🚀 다음 단계

1. **즉시 적용**: PostEntity의 fetch 타입을 LAZY로 변경
2. **즉시 적용**: PostRepository에서 @EntityGraph 제거
3. **선택 적용**: @BatchSize 추가 (성능 최적화)
4. **테스트**: 페이징이 정상 작동하는지 확인

---

**검토 일시**: 2025-12-13  
**결론**: 주요 에러는 해결되었으나, Hibernate 쿼리 최적화가 필요함

