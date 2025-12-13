# MySQL 컨테이너 접근 명령어 검토 및 가이드

## 📊 현재 컨테이너 정보

- **컨테이너 이름**: `spring-sns-mysql`
- **컨테이너 ID**: `edb81c62aa7f`
- **이미지**: `mysql:8.0`
- **상태**: `Up 5 hours (healthy)`
- **포트**: `0.0.0.0:3306->3306/tcp`

## ✅ 올바른 접근 명령어

### 방법 1: MySQL CLI로 접속 (권장)

```bash
# 기본 접속 (비밀번호 입력 필요)
docker exec -it spring-sns-mysql mysql -u root -p

# 비밀번호를 명령어에 포함 (보안 주의)
docker exec -it spring-sns-mysql mysql -u root -p'!Blackser7789'
```

**설명**:
- `docker exec -it`: 컨테이너 내부에서 대화형 명령 실행
- `spring-sns-mysql`: 컨테이너 이름
- `mysql -u root -p`: MySQL root 사용자로 접속
- `-p'!Blackser7789'`: 비밀번호 지정 (특수문자 포함 시 따옴표 필수)

### 방법 2: 한 줄 명령어로 실행

```bash
# 데이터베이스 목록 확인
docker exec spring-sns-mysql mysql -u root -p'!Blackser7789' -e "SHOW DATABASES;"

# 테이블 목록 확인
docker exec spring-sns-mysql mysql -u root -p'!Blackser7789' member -e "SHOW TABLES;"

# 특정 테이블 데이터 확인
docker exec spring-sns-mysql mysql -u root -p'!Blackser7789' member -e "SELECT * FROM member_entity LIMIT 10;"
```

### 방법 3: Docker Compose 사용

```bash
cd ~/포트폴리오/spring_sns_git/inhatc
docker compose exec mysql mysql -u root -p'!Blackser7789'
```

## ⚠️ 주의사항

### 1. 비밀번호 특수문자 처리

비밀번호에 `!`가 포함되어 있으므로 **반드시 따옴표로 감싸야 합니다**:

```bash
# ✅ 올바른 방법
docker exec -it spring-sns-mysql mysql -u root -p'!Blackser7789'

# ❌ 잘못된 방법 (특수문자 해석 오류)
docker exec -it spring-sns-mysql mysql -u root -p!Blackser7789
```

### 2. 컨테이너 이름 확인

컨테이너 이름이 정확한지 확인:

```bash
docker ps --filter "name=spring-sns-mysql"
```

### 3. 컨테이너 실행 상태 확인

컨테이너가 실행 중이어야 합니다:

```bash
docker ps | grep spring-sns-mysql
```

## 🔍 환경 변수 확인

현재 설정된 MySQL 환경 변수:

```bash
# 환경 변수 확인
docker inspect spring-sns-mysql --format='{{range .Config.Env}}{{println .}}{{end}}' | grep MYSQL
```

**예상 결과**:
- `MYSQL_ROOT_PASSWORD=!Blackser7789`
- `MYSQL_DATABASE=member`
- `MYSQL_USER=user`
- `MYSQL_PASSWORD=!Blackser7789`

## 📋 실용적인 명령어 모음

### 빠른 확인

```bash
# 1. 데이터베이스 목록
docker exec spring-sns-mysql mysql -u root -p'!Blackser7789' -e "SHOW DATABASES;"

# 2. 테이블 목록
docker exec spring-sns-mysql mysql -u root -p'!Blackser7789' member -e "SHOW TABLES;"

# 3. 각 테이블의 레코드 수
docker exec spring-sns-mysql mysql -u root -p'!Blackser7789' member -e "
SELECT TABLE_NAME, TABLE_ROWS 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'member';"

# 4. 특정 테이블 데이터 (member_entity)
docker exec spring-sns-mysql mysql -u root -p'!Blackser7789' member -e "SELECT * FROM member_entity LIMIT 10;"
```

### 대화형 접속

```bash
# MySQL 접속
docker exec -it spring-sns-mysql mysql -u root -p'!Blackser7789'

# 접속 후 SQL 명령어 실행:
# SHOW DATABASES;
# USE member;
# SHOW TABLES;
# SELECT * FROM member_entity LIMIT 10;
# EXIT;
```

## 🚨 일반적인 오류 및 해결

### 오류 1: "No such container"

```bash
# 해결: 컨테이너 이름 확인
docker ps -a | grep mysql
```

### 오류 2: "Access denied"

```bash
# 해결: 비밀번호 확인 및 따옴표 사용
docker exec -it spring-sns-mysql mysql -u root -p'!Blackser7789'
```

### 오류 3: "Container is not running"

```bash
# 해결: 컨테이너 시작
docker start spring-sns-mysql
# 또는
cd ~/포트폴리오/spring_sns_git/inhatc
docker compose up -d mysql
```

## ✅ 검증 명령어

현재 설정이 올바른지 확인:

```bash
# 1. 컨테이너 상태 확인
docker ps --filter "name=spring-sns-mysql"

# 2. 환경 변수 확인
docker inspect spring-sns-mysql --format='{{range .Config.Env}}{{println .}}{{end}}' | grep MYSQL_ROOT_PASSWORD

# 3. 접속 테스트
docker exec spring-sns-mysql mysql -u root -p'!Blackser7789' -e "SELECT 1;" && echo "✅ 접속 성공"
```

## 📝 요약

### 올바른 명령어

```bash
# 대화형 접속
docker exec -it spring-sns-mysql mysql -u root -p'!Blackser7789'

# 한 줄 명령어
docker exec spring-sns-mysql mysql -u root -p'!Blackser7789' -e "SHOW DATABASES;"
```

### 핵심 포인트

1. ✅ 컨테이너 이름: `spring-sns-mysql`
2. ✅ 사용자: `root`
3. ✅ 비밀번호: `!Blackser7789` (따옴표 필수)
4. ✅ 데이터베이스: `member`
5. ✅ 포트: `3306` (컨테이너 내부)

---

**작성일**: 2025-12-13  
**컨테이너**: spring-sns-mysql  
**MySQL 버전**: 8.0

