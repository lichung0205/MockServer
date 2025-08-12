// bridge/bridge.js
// WebSocket <-> TCP Bridge  by 瓶仔
const WebSocket = require('ws');
const net = require('net');

// ---- 可調參數  by 瓶仔 ----
const WS_PORT = 8080;                  // Web 端連這個 ws://localhost:8080/ws
const SERVER_HOST = '127.0.0.1';       // ClassroomServer 的 IP
const SERVER_PORT = 9527;              // ClassroomServer 的 Port
const GM_ID = 'neil';                  // teacher.properties 對應的 GM 資訊
const GM_NAME = 'gogoro';

// ---- 啟動 WS Server  by 瓶仔 ----
const wss = new WebSocket.Server({ port: WS_PORT, path: '/ws' }, () => {
    console.log(`[bridge] WS listening on ws://localhost:${WS_PORT}/ws`);
});

wss.on('connection', (ws) => {
    console.log('[bridge] WS connected from web client');

    // 建立到 ClassroomServer 的 TCP 連線  by 瓶仔
    const tcp = net.createConnection({ host: SERVER_HOST, port: SERVER_PORT }, () => {
        console.log('[bridge] TCP connected to ClassroomServer');
    });

    let loggedIn = false;

    // WS -> TCP
    ws.on('message', (data) => {
        try {
            const msg = typeof data === 'string' ? JSON.parse(data) : JSON.parse(data.toString());

            // 1) 先處理 login：只做一次
            if (!loggedIn && msg?.type === 'login') {
                const loginJson = JSON.stringify({
                    authType: 'TEACHER',
                    id: msg.id,       // ← 來自 Web
                    name: msg.name,   // ← 來自 Web
                });
                tcp.write(loginJson + '\n');
                loggedIn = true;
                return;
            }

            // 2) 其他指令（如 broadcast）必須在登入後才允許
            if (loggedIn && msg?.type) {
                tcp.write(JSON.stringify({
                    type: msg.type,
                    target: msg.target ?? '',
                    content: msg.content ?? '',
                }) + '\n');
            }

        } catch (e) {
            console.error('[bridge] WS message parse error:', e.message);
        }
    });

    // TCP -> WS：把 Server 的每一行回傳轉推給 Web 端  by 瓶仔
    let buffer = '';
    tcp.on('data', (chunk) => {
        buffer += chunk.toString('utf8');
        let idx;
        while ((idx = buffer.indexOf('\n')) >= 0) {
            const line = buffer.slice(0, idx);
            buffer = buffer.slice(idx + 1);
            if (line.trim().length > 0) {
                ws.send(line); // 直接轉文字，前端 MessageLog 可顯示
            }
        }
    });

    tcp.on('error', (err) => {
        console.error('[bridge] TCP error:', err.message);
        try { ws.send(`[系統] 與 ClassroomServer 連線失敗: ${err.message}`); } catch { }
        ws.close();
    });

    tcp.on('close', () => {
        console.log('[bridge] TCP closed');
        try { ws.close(); } catch { }
    });

    ws.on('close', () => {
        console.log('[bridge] WS closed');
        try { tcp.end(); } catch { }
    });
});
