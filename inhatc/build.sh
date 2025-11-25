#!/bin/bash

# Spring Boot 프로젝트 빌드 스크립트
# 사용법: ./build.sh [profile]
# 예시: ./build.sh prod

set -e  # 에러 발생 시 스크립트 중단

PROFILE=${1:-prod}
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
JAR_NAME="inhatc-0.0.1-SNAPSHOT.jar"

echo "=========================================="
echo "Spring Boot 프로젝트 빌드 시작"
echo "프로파일: $PROFILE"
echo "프로젝트 디렉토리: $PROJECT_DIR"
echo "=========================================="

# Maven 빌드
cd "$PROJECT_DIR"
echo "Maven 빌드 실행 중..."
mvn clean package -DskipTests -P$PROFILE

# 빌드 결과 확인
if [ -f "target/$JAR_NAME" ]; then
    echo "=========================================="
    echo "[OK] 빌드 성공!"
    echo "JAR 파일 위치: target/$JAR_NAME"
    echo "파일 크기: $(du -h target/$JAR_NAME | cut -f1)"
    echo "=========================================="
    
    # JAR 파일 실행 권한 부여
    chmod +x target/$JAR_NAME
    
    exit 0
else
    echo "=========================================="
    echo "[ERROR] 빌드 실패: JAR 파일을 찾을 수 없습니다."
    echo "=========================================="
    exit 1
fi



