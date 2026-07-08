const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 3456;

const server = http.createServer((req, res) => {
  // CORS 헤더 설정
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  if (req.method === 'POST' && req.url === '/save') {
    let body = '';
    req.on('data', chunk => {
      body += chunk.toString();
    });

    req.on('end', () => {
      try {
        const data = JSON.parse(body);
        const { filename, base64 } = data;

        if (!filename || !base64) {
          res.writeHead(400, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ error: 'Missing filename or base64 data' }));
          return;
        }

        // base64 데이터 디코딩
        const base64Data = base64.replace(/^data:image\/png;base64,/, "");
        const targetPath = path.resolve(__dirname, '..', filename);

        // 상위 디렉토리가 존재하는지 확인 후 저장
        const dir = path.dirname(targetPath);
        if (!fs.existsSync(dir)) {
          fs.mkdirSync(dir, { recursive: true });
        }

        fs.writeFileSync(targetPath, base64Data, 'base64');
        console.log(`Saved: ${targetPath}`);

        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: true, path: targetPath }));
      } catch (err) {
        console.error(err);
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: err.message }));
      }
    });
  } else {
    // GET convert.html 서빙
    if (req.method === 'GET' && (req.url === '/' || req.url === '/index.html' || req.url === '/convert.html')) {
      const htmlPath = path.join(__dirname, 'convert.html');
      if (fs.existsSync(htmlPath)) {
        res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
        res.end(fs.readFileSync(htmlPath));
      } else {
        res.writeHead(404);
        res.end('convert.html not found');
      }
    } else if (req.method === 'GET' && (req.url === '/icon.svg' || req.url === '/icon_round.svg')) {
      const svgPath = path.join(__dirname, req.url);
      if (fs.existsSync(svgPath)) {
        res.writeHead(200, { 'Content-Type': 'image/svg+xml' });
        res.end(fs.readFileSync(svgPath));
      } else {
        res.writeHead(404);
        res.end('SVG not found');
      }
    } else {
      res.writeHead(404);
      res.end('Not Found');
    }
  }
});

server.listen(PORT, () => {
  console.log(`Convert server listening on http://localhost:${PORT}`);
});
