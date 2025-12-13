# MySQL 컨테이너 데이터베이스 확인 방법

## 컨테이너 정보
- 컨테이너 이름: `spring-sns-mysql`
- 컨테이너 ID: `edb81c62aa7f`
- 이미지: `mysql:8.0`
- 포트: `3306`

## 방법 1: Docker exec로 MySQL 접속 (권장)

### 1. MySQL CLI로 접속
```bash
docker exec -it spring-sns-mysql mysql -u root -p
# 비밀번호 입력: !Blackser7789
```

### 2. 데이터베이스 목록 확인
```sql
SHOW DATABASES;
```

### 3. 데이터베이스 선택
```sql
USE member;
```

### 4. 테이블 목록 확인
```sql
SHOW TABLES;
```

### 5. 테이블 데이터 확인
```sql
-- 모든 테이블의 데이터 개수 확인
SELECT 
    TABLE_NAME,
    TABLE_ROWS
FROM 
    information_schema.TABLES
WHERE 
    TABLE_SCHEMA = 'member';

-- 특정 테이블 데이터 확인 (예: member_entity)
SELECT * FROM member_entity LIMIT 10;

-- 특정 테이블 데이터 확인 (예: post_entity)
SELECT * FROM post_entity LIMIT 10;

-- 특정 테이블 데이터 확인 (예: comment_entity)
SELECT * FROM comment_entity LIMIT 10;
```

## 방법 2: 한 줄 명령어로 실행

### 데이터베이스 목록 확인
```bash
docker exec -it spring-sns-mysql mysql -u root -p'!Blackser7789' -e "SHOW DATABASES;"
```

### 테이블 목록 확인
```bash
docker exec -it spring-sns-mysql mysql -u root -p'!Blackser7789' member -e "SHOW TABLES;"
```

### 특정 테이블 데이터 확인
```bash
# member_entity 테이블 확인
docker exec -it spring-sns-mysql mysql -u root -p'!Blackser7789' member -e "SELECT * FROM member_entity LIMIT 10;"

# post_entity 테이블 확인
docker exec -it spring-sns-mysql mysql -u root -p'!Blackser7789' member -e "SELECT * FROM post_entity LIMIT 10;"

# comment_entity 테이블 확인
docker exec -it spring-sns-mysql mysql -u root -p'!Blackser7789' member -e "SELECT * FROM comment_entity LIMIT 10;"
```

## 방법 3: MySQL Workbench 또는 DBeaver 사용

### 연결 정보
- Host: `localhost` (또는 서버 IP)
- Port: `3306`
- Username: `root`
- Password: `!Blackser7789`
- Database: `member`

## 방법 4: 스크립트로 전체 데이터 확인

```bash
#!/bin/bash
# mysql-dump.sh

echo "=== 데이터베이스 목록 ==="
docker exec spring-sns-mysql mysql -u root -p'!Blackser7789' -e "SHOW DATABASES;"

echo ""
echo "=== member 데이터베이스의 테이블 목록 ==="
docker exec spring-sns-mysql mysql -u root -p'!Blackser7789' member -e "SHOW TABLES;"

echo ""
echo "=== 각 테이블의 데이터 개수 ==="
docker exec spring-sns-mysql mysql -u root -p'!Blackser7789' member -e "
SELECT 
    TABLE_NAME,
    TABLE_ROWS
FROM 
    information_schema.TABLES
WHERE 
    TABLE_SCHEMA = 'member'
ORDER BY TABLE_NAME;
"
```

## 빠른 확인 명령어 모음

```bash
# 1. 데이터베이스 목록
docker exec spring-sns-mysql mysql -u root -p'!Blackser7789' -e "SHOW DATABASES;"

# 2. 테이블 목록
docker exec spring-sns-mysql mysql -u root -p'!Blackser7789' member -e "SHOW TABLES;"

# 3. 모든 테이블의 레코드 수
docker exec spring-sns-mysql mysql -u root -p'!Blackser7789' member -e "
SELECT TABLE_NAME, TABLE_ROWS 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'member';"

# 4. 특정 테이블 데이터 (member_entity 예시)
docker exec spring-sns-mysql mysql -u root -p'!Blackser7789' member -e "SELECT * FROM member_entity;"

# 5. 특정 테이블 데이터 (post_entity 예시)
docker exec spring-sns-mysql mysql -u root -p'!Blackser7789' member -e "SELECT * FROM post_entity LIMIT 20;"
```

## 주의사항

1. **비밀번호에 특수문자 포함**: `!Blackser7789`는 따옴표로 감싸야 합니다
2. **대소문자 구분**: MySQL은 기본적으로 대소문자를 구분하지 않지만, 테이블 이름은 확인 필요
3. **데이터가 많은 경우**: `LIMIT`을 사용하여 일부만 확인하는 것이 좋습니다


