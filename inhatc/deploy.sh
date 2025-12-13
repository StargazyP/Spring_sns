#!/bin/bash

# 우분투 서버 배포 스크립트
# 사용법: ./deploy.sh [서버주소] [사용자명] [배포경로]
# 예시: ./deploy.sh 192.168.1.100 ubuntu /opt/inhatc

set -e

SERVER=${1:-localhost}
USER=${2:-ubuntu}
DEPLOY_PATH=${3:-/opt/inhatc}
JAR_NAME="inhatc-0.0.1-SNAPSHOT.jar"
SERVICE_NAME="inhatc"

echo "=========================================="
echo "Spring Boot 애플리케이션 배포"
echo "서버: $USER@$SERVER"
echo "배포 경로: $DEPLOY_PATH"
echo "=========================================="

# 로컬에서 빌드
echo "1. 로컬 빌드 실행..."
./build.sh prod

# 서버에 디렉토리 생성
echo "2. 서버 디렉토리 생성..."
ssh $USER@$SERVER "sudo mkdir -p $DEPLOY_PATH/{app,logs,uploads/{posts,profiles,messages}} && sudo chown -R $USER:$USER $DEPLOY_PATH"

# JAR 파일 업로드
echo "3. JAR 파일 업로드..."
scp target/$JAR_NAME $USER@$SERVER:$DEPLOY_PATH/app/

# 설정 파일 업로드 (있는 경우)
if [ -f "application-prod.properties" ]; then
    echo "4. 설정 파일 업로드..."
    scp src/main/resources/application-prod.properties $USER@$SERVER:$DEPLOY_PATH/app/
fi

# systemd 서비스 파일 업로드
echo "5. systemd 서비스 파일 업로드..."
scp deploy/inhatc.service $USER@$SERVER:/tmp/

# 서버에서 서비스 설정
echo "6. 서버에서 서비스 설정..."
ssh $USER@$SERVER << EOF
    sudo mv /tmp/inhatc.service /etc/systemd/system/
    sudo sed -i "s|DEPLOY_PATH|$DEPLOY_PATH|g" /etc/systemd/system/inhatc.service
    sudo systemctl daemon-reload
    sudo systemctl enable $SERVICE_NAME
    sudo systemctl restart $SERVICE_NAME
    sudo systemctl status $SERVICE_NAME
EOF

echo "=========================================="
echo "[OK] 배포 완료!"
echo "서비스 상태 확인: ssh $USER@$SERVER 'sudo systemctl status $SERVICE_NAME'"
echo "로그 확인: ssh $USER@$SERVER 'sudo journalctl -u $SERVICE_NAME -f'"
echo "=========================================="



