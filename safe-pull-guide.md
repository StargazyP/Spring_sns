# 안전한 Git Pull 가이드

## 현재 상황
- 원격 저장소에 force push가 있었습니다
- 많은 파일이 원격에서 삭제되었습니다
- 로컬에 수정된 파일이 있습니다 (`inhatc/docker-compose.yml`)

## 방법 1: 현재 변경사항 백업 후 Pull (권장)

```bash
# 1. 현재 변경사항 커밋 또는 스태시
git add .
git commit -m "로컬 변경사항 백업"

# 또는 스태시 사용
git stash save "로컬 변경사항 백업"

# 2. 원격 변경사항 확인
git fetch origin
git log HEAD..origin/main --oneline

# 3. Pull 실행
git pull origin main

# 4. 스태시를 사용했다면 복원
git stash pop
```

## 방법 2: 로컬 변경사항 유지하면서 Pull

```bash
# 1. 현재 변경사항 커밋
git add .
git commit -m "로컬 변경사항"

# 2. Pull (병합)
git pull origin main

# 3. 충돌이 발생하면 해결
# 충돌 파일을 수동으로 편집 후
git add <충돌해결한파일>
git commit
```

## 방법 3: 원격 상태로 완전히 동기화 (주의!)

```bash
# ⚠️ 경고: 로컬 변경사항이 모두 사라집니다!

# 1. 현재 변경사항 백업 (선택사항)
git stash save "백업"

# 2. 원격 상태로 강제 동기화
git fetch origin
git reset --hard origin/main

# 3. 백업 복원 (필요시)
git stash pop
```

## 방법 4: 특정 파일만 보존하면서 Pull

```bash
# 1. 보존할 파일을 다른 곳에 복사
cp -r inhatc/docker-compose.yml /tmp/docker-compose.yml.backup

# 2. Pull 실행
git pull origin main

# 3. 보존한 파일 복원
cp /tmp/docker-compose.yml.backup inhatc/docker-compose.yml
git add inhatc/docker-compose.yml
git commit -m "docker-compose.yml 복원"
```

## 원격에서 삭제될 주요 파일들

다음 파일들이 원격에서 삭제되어 pull 시 로컬에서도 삭제됩니다:
- `.github/workflows/ci-cd.yml`
- `.github/workflows/README.md`
- `DEPLOYMENT.md`
- `README-MYSQL-SETUP.md`
- `inhatc/Dockerfile`
- `inhatc/docker-compose.yml`
- `inhatc/.dockerignore`
- `inhatc/src/test/resources/application-test.properties`
- `node_modules/` 전체
- `package.json`, `package-lock.json`
- `webhook-server.js`

## 권장 사항

1. **중요한 로컬 변경사항을 먼저 커밋하세요**
2. **원격 변경사항을 확인한 후 pull하세요**
3. **필요한 파일은 백업하세요**

