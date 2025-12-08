#!/bin/bash

# 빠른 MySQL 설치 스크립트 (서버에서 직접 실행)
# SSH 서버에서 실행: bash <(curl -s https://raw.githubusercontent.com/your-repo/setup-mysql.sh) 또는 이 파일을 서버에 업로드

set -e

MYSQL_ROOT_PASSWORD="blackser7789"
MYSQL_DB_NAME="member"
MYSQL_USER="spring_sns_user"
MYSQL_USER_PASSWORD="blackser7789"

echo "MySQL 설치 시작..."

# MySQL 설치
sudo apt-get update -qq
sudo DEBIAN_FRONTEND=noninteractive apt-get install -y mysql-server

# MySQL 시작
sudo systemctl start mysql
sudo systemctl enable mysql

# Root 비밀번호 설정
sudo mysql <<EOF
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '${MYSQL_ROOT_PASSWORD}';
FLUSH PRIVILEGES;
EOF

# 데이터베이스 및 사용자 생성
sudo mysql -u root -p${MYSQL_ROOT_PASSWORD} <<EOF
CREATE DATABASE IF NOT EXISTS ${MYSQL_DB_NAME} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '${MYSQL_USER}'@'localhost' IDENTIFIED BY '${MYSQL_USER_PASSWORD}';
CREATE USER IF NOT EXISTS '${MYSQL_USER}'@'%' IDENTIFIED BY '${MYSQL_USER_PASSWORD}';
GRANT ALL PRIVILEGES ON ${MYSQL_DB_NAME}.* TO '${MYSQL_USER}'@'localhost';
GRANT ALL PRIVILEGES ON ${MYSQL_DB_NAME}.* TO '${MYSQL_USER}'@'%';
FLUSH PRIVILEGES;
EOF

# 원격 접속 허용
sudo sed -i 's/bind-address.*/bind-address = 0.0.0.0/' /etc/mysql/mysql.conf.d/mysqld.cnf 2>/dev/null || \
sudo sed -i 's/bind-address.*/bind-address = 0.0.0.0/' /etc/mysql/my.cnf 2>/dev/null || true

# 방화벽 설정
sudo ufw allow 3306/tcp 2>/dev/null || true

# MySQL 재시작
sudo systemctl restart mysql

echo "MySQL 설치 완료!"
echo "데이터베이스: ${MYSQL_DB_NAME}"
echo "사용자: ${MYSQL_USER}"
echo "비밀번호: ${MYSQL_USER_PASSWORD}"

