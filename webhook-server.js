// webhook-server.js
const express = require('express');
const { exec } = require('child_process');
const crypto = require('crypto');

const app = express();
const PORT = 3000;
// 환경 변수에서 시크릿 가져오기 (없으면 기본값 사용)
const SECRET = process.env.WEBHOOK_SECRET || 'your_webhook_secret_here';

// 로깅 미들웨어
app.use((req, res, next) => {
  console.log(`[${new Date().toISOString()}] ${req.method} ${req.path}`);
  next();
});

app.use(express.json({
  verify: (req, res, buf) => {
    // 시크릿이 설정되지 않았으면 검증 건너뛰기
    if (SECRET === 'your_webhook_secret_here') {
      console.log('Webhook secret이 설정되지 않았습니다. 검증을 건너뜁니다.');
      return;
    }
    
    const signature = req.headers['x-hub-signature-256'];
    if (signature) {
      const hmac = crypto.createHmac('sha256', SECRET);
      const digest = 'sha256=' + hmac.update(buf).digest('hex');
      if (signature !== digest) {
        console.error('Invalid signature');
        throw new Error('Invalid signature');
      }
      console.log('Signature verified');
    }
  }
}));

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

app.post('/webhook', (req, res) => {
  const event = req.headers['x-github-event'];
  console.log(`Webhook event received: ${event}`);
  
  if (event === 'push') {
    console.log('Push event received, updating Spring Boot container...');
    
    // 여러 가능한 디렉토리 경로 시도 (Git 저장소 경로와 Docker Compose 경로)
    const possiblePaths = [
      {
        gitPath: '/home/jangdonggun/포트폴리오/spring_sns_git',
        composePath: '/home/jangdonggun/포트폴리오/spring_sns_git/inhatc'
      },
      {
        gitPath: '/home/jangdonggun/spring_sns_git',
        composePath: '/home/jangdonggun/spring_sns_git/inhatc'
      },
      {
        gitPath: '/home/jangdonggun/포트폴리오/Spring_sns',
        composePath: '/home/jangdonggun/포트폴리오/Spring_sns'
      }
    ];
    
    let deployCommand = '';
    for (const paths of possiblePaths) {
      deployCommand += `
        if [ -d "${paths.gitPath}" ] && [ -d "${paths.composePath}" ]; then
          echo "📂 Git 저장소: ${paths.gitPath}" &&
          echo "📂 Docker Compose 디렉토리: ${paths.composePath}" &&
          cd "${paths.gitPath}" &&
          echo "📥 최신 코드 가져오기..." &&
          git fetch origin &&
          # 안전한 병합: 로컬 변경사항 보존
          git stash || true &&
          git merge origin/main || {
            echo "병합 충돌 발생, rebase 시도..." &&
            git merge --abort 2>/dev/null || true &&
            git rebase origin/main || {
              echo "병합 실패, 현재 상태 유지" &&
              git rebase --abort 2>/dev/null || true &&
              exit 1
            }
          } &&
          # stash한 변경사항 복원 (이미지 파일 등)
          git stash pop || true &&
          echo "🐳 Docker 이미지 업데이트..." &&
          cd "${paths.composePath}" &&
          docker compose pull app 2>/dev/null || docker-compose pull app 2>/dev/null || echo "⚠️ docker compose pull 실패" &&
          docker compose up -d --build 2>/dev/null || docker-compose up -d --build 2>/dev/null &&
          docker compose ps 2>/dev/null || docker-compose ps 2>/dev/null &&
          echo "✅ 배포 완료!" &&
          exit 0
        fi
      `;
    }
    deployCommand += 'echo "프로젝트 디렉토리를 찾을 수 없습니다" && exit 1';
    
    exec(deployCommand, { maxBuffer: 1024 * 1024 * 10 }, (err, stdout, stderr) => {
      if (err) {
        console.error('배포 오류:', err);
        console.error('stderr:', stderr);
        res.status(500).json({ error: 'Deployment failed', message: err.message });
      } else {
        console.log('배포 성공!');
        console.log('stdout:', stdout);
        if (stderr) console.log('stderr:', stderr);
        res.status(200).json({ 
          success: true, 
          message: 'Deployment completed',
          output: stdout 
        });
      }
    });
  } else {
    console.log(`ℹEvent ${event}는 처리하지 않습니다.`);
    res.status(200).json({ message: `Event ${event} received but not processed` });
  }
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`Webhook server running on port ${PORT}`);
  console.log(`Listening on http://0.0.0.0:${PORT}/webhook`);
  console.log(`Health check: http://0.0.0.0:${PORT}/health`);
});

