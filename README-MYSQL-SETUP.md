# MySQL 설치 및 설정 가이드

## SSH 서버에 MySQL 설치하기

### 방법 1: 자동 설치 스크립트 사용 (권장)

1. **스크립트를 서버에 업로드**
```bash
# 로컬에서 서버로 스크립트 복사
scp setup-mysql.sh ubuntu@jangdonggun.iptime.org:~/
```

2. **서버에 SSH 접속**
```bash
ssh ubuntu@jangdonggun.iptime.org
```

3. **스크립트 실행 권한 부여 및 실행**
```bash
chmod +x ~/setup-mysql.sh
sudo ~/setup-mysql.sh
```

### 방법 2: 수동 설치

#### 1. MySQL 설치
```bash
ssh ubuntu@jangdonggun.iptime.org

# 패키지 업데이트
sudo apt-get update

# MySQL 설치
sudo apt-get install -y mysql-server

# MySQL 서비스 시작
sudo systemctl start mysql
sudo systemctl enable mysql
```

#### 2. MySQL Root 비밀번호 설정
```bash
sudo mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'blackser7789';"
sudo mysql -e "FLUSH PRIVILEGES;"
```

#### 3. 데이터베이스 생성
```bash
sudo mysql -u root -pblackser7789 <<EOF
CREATE DATABASE IF NOT EXISTS member CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EOF
```

#### 4. 사용자 생성 및 권한 부여
```bash
sudo mysql -u root -pblackser7789 <<EOF
CREATE USER IF NOT EXISTS 'spring_sns_user'@'localhost' IDENTIFIED BY 'blackser7789';
CREATE USER IF NOT EXISTS 'spring_sns_user'@'%' IDENTIFIED BY 'blackser7789';
GRANT ALL PRIVILEGES ON member.* TO 'spring_sns_user'@'localhost';
GRANT ALL PRIVILEGES ON member.* TO 'spring_sns_user'@'%';
FLUSH PRIVILEGES;
EOF
```

#### 5. 원격 접속 허용 설정
```bash
# MySQL 설정 파일 수정
sudo sed -i 's/bind-address.*/bind-address = 0.0.0.0/' /etc/mysql/mysql.conf.d/mysqld.cnf

# 또는 직접 편집
sudo nano /etc/mysql/mysql.conf.d/mysqld.cnf
# bind-address = 127.0.0.1 을 bind-address = 0.0.0.0 으로 변경

# MySQL 재시작
sudo systemctl restart mysql
```

#### 6. 방화벽 포트 열기 (필요한 경우)
```bash
# UFW 사용 시
sudo ufw allow 3306/tcp
sudo ufw reload
```

## 연결 테스트

```bash
# 로컬에서 연결 테스트
mysql -u spring_sns_user -pblackser7789 -h jangdonggun.iptime.org member

# 서버에서 연결 테스트
mysql -u spring_sns_user -pblackser7789 member
```

## Spring Boot 설정

`application.properties` 파일이 이미 올바르게 설정되어 있습니다:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/member?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
spring.datasource.username=spring_sns_user
spring.datasource.password=blackser7789
```

## Docker Compose 사용 시

Docker Compose를 사용하는 경우, `docker-compose.yml`에서 MySQL 서비스가 이미 설정되어 있습니다.

## 문제 해결

### MySQL 접속 실패
```bash
# MySQL 상태 확인
sudo systemctl status mysql

# MySQL 로그 확인
sudo tail -f /var/log/mysql/error.log
```

### 권한 문제
```bash
# 사용자 권한 확인
sudo mysql -u root -pblackser7789 -e "SHOW GRANTS FOR 'spring_sns_user'@'localhost';"
```

### 포트 확인
```bash
# MySQL 포트 확인
sudo netstat -tlnp | grep 3306
```

