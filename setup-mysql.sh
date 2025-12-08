#!/bin/bash

# MySQL 설치 및 설정 스크립트
# 사용법: ./setup-mysql.sh

set -e

echo "=========================================="
echo "MySQL 설치 및 설정 시작"
echo "=========================================="

# MySQL 비밀번호 설정
MYSQL_ROOT_PASSWORD="blackser7789"
MYSQL_DB_NAME="member"
MYSQL_USER="spring_sns_user"
MYSQL_USER_PASSWORD="blackser7789"

# 1. MySQL 설치
echo "1. MySQL 설치 중..."
sudo apt-get update
sudo apt-get install -y mysql-server

# 2. MySQL 서비스 시작
echo "2. MySQL 서비스 시작 중..."
sudo systemctl start mysql
sudo systemctl enable mysql

# 3. MySQL 보안 설정 (초기 비밀번호 설정)
echo "3. MySQL 보안 설정 중..."
sudo mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '${MYSQL_ROOT_PASSWORD}';"
sudo mysql -e "FLUSH PRIVILEGES;"

# 4. 데이터베이스 생성
echo "4. 데이터베이스 생성 중..."
sudo mysql -u root -p${MYSQL_ROOT_PASSWORD} <<EOF
CREATE DATABASE IF NOT EXISTS ${MYSQL_DB_NAME} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EOF

# 5. 사용자 생성 및 권한 부여
echo "5. 사용자 생성 및 권한 부여 중..."
sudo mysql -u root -p${MYSQL_ROOT_PASSWORD} <<EOF
CREATE USER IF NOT EXISTS '${MYSQL_USER}'@'localhost' IDENTIFIED BY '${MYSQL_USER_PASSWORD}';
CREATE USER IF NOT EXISTS '${MYSQL_USER}'@'%' IDENTIFIED BY '${MYSQL_USER_PASSWORD}';
GRANT ALL PRIVILEGES ON ${MYSQL_DB_NAME}.* TO '${MYSQL_USER}'@'localhost';
GRANT ALL PRIVILEGES ON ${MYSQL_DB_NAME}.* TO '${MYSQL_USER}'@'%';
FLUSH PRIVILEGES;
EOF

# 6. MySQL 원격 접속 허용 설정
echo "6. MySQL 원격 접속 설정 중..."
sudo sed -i 's/bind-address.*/bind-address = 0.0.0.0/' /etc/mysql/mysql.conf.d/mysqld.cnf || \
sudo sed -i 's/bind-address.*/bind-address = 0.0.0.0/' /etc/mysql/my.cnf

# 7. MySQL 서비스 재시작
echo "7. MySQL 서비스 재시작 중..."
sudo systemctl restart mysql

# 8. 방화벽 설정 (UFW 사용 시)
if command -v ufw &> /dev/null; then
    echo "8. 방화벽 포트 열기 중..."
    sudo ufw allow 3306/tcp
fi

echo "=========================================="
echo "MySQL 설치 및 설정 완료!"
echo "=========================================="
echo "데이터베이스 이름: ${MYSQL_DB_NAME}"
echo "사용자: ${MYSQL_USER}"
echo "비밀번호: ${MYSQL_USER_PASSWORD}"
echo "Root 비밀번호: ${MYSQL_ROOT_PASSWORD}"
echo "=========================================="

# 연결 테스트
echo "연결 테스트 중..."
sudo mysql -u ${MYSQL_USER} -p${MYSQL_USER_PASSWORD} -e "USE ${MYSQL_DB_NAME}; SELECT 'Connection successful!' AS result;"

echo "설정이 완료되었습니다!"

