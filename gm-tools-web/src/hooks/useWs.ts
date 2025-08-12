// 自動重新連線

import { useEffect, useRef, useState } from 'react';

type Opts = {
    url: string;
    onOpen?: () => void;
    onClose?: (ev: CloseEvent) => void;
    onMessage?: (ev: MessageEvent) => void;
    autoReconnect?: boolean;
};

export function useWs({ url, onOpen, onClose, onMessage, autoReconnect = false }: Opts) {
    const wsRef = useRef<WebSocket | null>(null);
    const [status, setStatus] = useState<'idle' | 'connecting' | 'open' | 'closed'>('idle');
    const retryRef = useRef(0);
    const stopRetryRef = useRef(false);

    // 連線
    const connect = () => {
        stopRetryRef.current = false;
        if (wsRef.current && (wsRef.current.readyState === WebSocket.OPEN || wsRef.current.readyState === WebSocket.CONNECTING)) return;

        setStatus('connecting');
        const ws = new WebSocket(url);
        wsRef.current = ws;

        ws.onopen = () => {
            retryRef.current = 0;
            setStatus('open');
            onOpen?.();
        };
        ws.onmessage = (ev) => onMessage?.(ev);
        ws.onclose = (ev) => {
            setStatus('closed');
            onClose?.(ev);
            wsRef.current = null;
            if (!stopRetryRef.current && autoReconnect) {
                const delay = Math.min(10000, 1000 * 2 ** retryRef.current++);
                setTimeout(() => connect(), delay);
            }
        };
        ws.onerror = () => ws.close();
    };
    // 斷線
    const disconnect = () => {
        stopRetryRef.current = true;
        wsRef.current?.close();
        wsRef.current = null;
    };
    // 發封包
    const send = (data: any) => {
        if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
            wsRef.current.send(typeof data === 'string' ? data : JSON.stringify(data));
            return true;
        }
        return false;
    };

    useEffect(() => () => { // unmount
        stopRetryRef.current = true;
        wsRef.current?.close();
    }, []);

    return { status, connect, disconnect, send };
}
