import { useEffect, useState } from 'react';
import AppLayout from '@/components/layout/AppLayout';
import StatusBar from '@/components/StatusBar';
import Menu from '@/components/Menu';
import MessageLog from '@/components/MessageLog';
import LoginForm from '@/components/LoginForm';
import BroadcastDialog from '@/dialogs/BroadcastDialog';
import CommentDialog from '@/dialogs/CommentDialog';
import ConfirmDialog from '@/dialogs/ConfirmDialog';
import { useAppStore } from '@/store/useAppStore';
import { ToastProvider, useToast } from '@/providers/ToastProvider';
import { useWs } from '@/hooks/useWs';

function AppInner() {
  const isLoggedIn = useAppStore(s => s.isLoggedIn);
  const isConnected = useAppStore(s => s.connected);
  const push = useAppStore(s => s.push);
  const reset = useAppStore(s => s.reset);
  const toast = useToast();

  const [openB, setOpenB] = useState(false);
  const [openC, setOpenC] = useState(false);
  const [openClear, setOpenClear] = useState(false);

  const ws = useWs({
    url: 'ws://localhost:8080/ws',        // 之後換真實 URL
    onOpen: () => { isConnected; toast.success('已連線'); },
    onClose: () => { !isConnected; toast.warning('連線中斷，嘗試重連中…'); },
    onMessage: (ev) => {
      // TODO: 解析後丟到 messages
      // push(...)
      console.log('WS message', ev.data);
    },
  });

  // 登入後自動連線；登出時（reset）要確保斷線
  useEffect(() => {
    if (isLoggedIn) ws.connect();
    else ws.disconnect();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isLoggedIn]);

  const connect = () => ws.connect();
  const disconnect = () => { ws.disconnect(); reset(); toast.info('已斷線並登出'); };

  return (
    <>
      <AppLayout
        statusBar={<StatusBar onConnect={connect} onDisconnect={disconnect} />}
        left={
          <Menu
            onBroadcast={() => setOpenB(true)}
            onComment={() => setOpenC(true)}
            onClear={() => setOpenClear(true)}
          />
        }
        center={isLoggedIn ? <MessageLog /> : <LoginForm />}
      />

      <BroadcastDialog open={openB} onClose={() => setOpenB(false)} />
      <CommentDialog open={openC} onClose={() => setOpenC(false)} />
      <ConfirmDialog
        open={openClear}
        onClose={() => setOpenClear(false)}
        title="清場"
        message="您是否要清理教室？"
        confirmText="是"
        cancelText="否"
        confirmColor="error"
        onConfirm={() => { push({ type: 'clear', at: Date.now(), confirm: true }); setOpenClear(false); }}
      />
    </>
  );
}

export default function App() {
  return (
    <ToastProvider>
      <AppInner />
    </ToastProvider>
  );
}
