// webhook-server.js
const express = require('express');
const { exec } = require('child_process');
const crypto = require('crypto');

const app = express();
const PORT = 3000;
const SECRET = 'your_webhook_secret_here';

app.use(express.json({
  verify: (req, res, buf) => {
    const signature = req.headers['x-hub-signature-256'];
    const hmac = crypto.createHmac('sha256', SECRET);
    const digest = 'sha256=' + hmac.update(buf).digest('hex');
    if (signature !== digest) {
      throw new Error('Invalid signature');
    }
  }
}));

app.post('/webhook', (req, res) => {
  const event = req.headers['x-github-event'];
  if (event === 'push') {
    console.log('Push event received, updating Spring Boot container...');
    exec(`
      cd /home/jangdonggun/포트폴리오/spring_sns_git &&
      git reset --hard origin/main &&
      docker-compose up -d --build
    `, (err, stdout, stderr) => {
      if (err) console.error(err);
      else console.log(stdout);
    });
  }
  res.sendStatus(200);
});

app.listen(PORT, () => console.log(`Webhook listener running on port ${PORT}`));

