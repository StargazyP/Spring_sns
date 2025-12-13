#!/bin/bash
# MySQL 컨테이너 빠른 확인 스크립트

CONTAINER="spring-sns-mysql"
DB_USER="root"
DB_PASS="!Blackser7789"
DB_NAME="member"

echo "=========================================="
echo "MySQL 컨테이너 데이터베이스 확인"
echo "=========================================="
echo ""

echo "1. 데이터베이스 목록:"
docker exec $CONTAINER mysql -u $DB_USER -p"$DB_PASS" -e "SHOW DATABASES;" 2>/dev/null || echo "접속 실패"
echo ""

echo "2. $DB_NAME 데이터베이스의 테이블 목록:"
docker exec $CONTAINER mysql -u $DB_USER -p"$DB_PASS" $DB_NAME -e "SHOW TABLES;" 2>/dev/null || echo "접속 실패"
echo ""

echo "3. 각 테이블의 레코드 수:"
docker exec $CONTAINER mysql -u $DB_USER -p"$DB_PASS" $DB_NAME -e "
SELECT 
    TABLE_NAME as '테이블명',
    TABLE_ROWS as '레코드 수'
FROM 
    information_schema.TABLES
WHERE 
    TABLE_SCHEMA = '$DB_NAME'
ORDER BY TABLE_NAME;" 2>/dev/null || echo "접속 실패"
echo ""

echo "=========================================="
echo "완료"
echo "=========================================="
