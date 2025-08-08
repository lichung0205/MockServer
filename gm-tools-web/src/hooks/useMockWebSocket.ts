// hooks/useMockWebSocket.ts
import { useState, useEffect } from 'react';

export default function useMockWebSocket() {
  const [isConnected, setIsConnected] = useState(false);
  const [logs, setLogs] = useState<string[]>([]);

  const mockSend = (action: string, payload?: any) => {
    const mockResponses: Record<string, string> = {
      login: "尼奧 老師已登入系統",
      broadcast: `廣播成功: ${payload.message}`,
      count: `當前在線人數: ${Math.floor(Math.random() * 10)}人`,
      find: `已通知學員 ${payload.target} 前來`
    };

    setLogs(prev => [...prev, `[操作] ${action}`, `[系統] ${mockResponses[action]}`]);
  };

  return { isConnected, logs, connect: () => setIsConnected(true), mockSend };
}