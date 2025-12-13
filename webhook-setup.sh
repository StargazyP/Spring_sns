#!/bin/bash

# Webhook 서버 설정 스크립트

set -e

echo "=========================================="
echo "Webhook 서버 설정"
echo "=========================================="

# 1. Node.js 설치 확인
echo "1. Node.js 확인 중..."
if ! command -v node &> /dev/null; then
    echo "⚠️ Node.js가 설치되어 있지 않습니다. 설치 중..."
    curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
    sudo apt-get install -y nodejs
fi
echo "✅ Node.js 버전: $(node --version)"
echo "✅ npm 버전: $(npm --version)"

# 2. 필요한 패키지 설치
echo "2. 필요한 패키지 설치 중..."
if [ -f package.json ]; then
    npm install
else
    npm init -y
    npm install express
fi
echo "✅ 패키지 설치 완료"

# 3. Webhook 서버 시작 (PM2 사용)
echo "3. Webhook 서버 시작 중..."
if ! command -v pm2 &> /dev/null; then
    echo "PM2 설치 중..."
    sudo npm install -g pm2
fi

# 기존 프로세스 중지
pm2 stop webhook-server 2>/dev/null || true
pm2 delete webhook-server 2>/dev/null || true

# Webhook 서버 시작
cd "$(dirname "$0")"
pm2 start webhook-server.js --name webhook-server
pm2 save
pm2 startup

echo "✅ Webhook 서버가 시작되었습니다!"
echo ""
echo "서버 상태 확인:"
pm2 status
echo ""
echo "로그 확인: pm2 logs webhook-server"
echo "서버 중지: pm2 stop webhook-server"
echo "서버 재시작: pm2 restart webhook-server"

