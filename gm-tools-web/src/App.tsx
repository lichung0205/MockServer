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
  const setConnected = useAppStore(s => s.setConnected);

  const account = useAppStore(s => s.account);
  const nickname = useAppStore(s => s.nickname);

  const push = useAppStore(s => s.push);
  const reset = useAppStore(s => s.reset);
  const toast = useToast();

  const [openB, setOpenB] = useState(false);
  const [openC, setOpenC] = useState(false);
  const [openClear, setOpenClear] = useState(false);

  const ws = useWs({
    url: 'ws://localhost:8080/ws',
    onOpen: () => {
      setConnected(true);
      // 連上就先送 login  // by 瓶仔
      ws.send({ type: 'login', id: account, name: nickname });
      toast.success('已連線');
    },
    onClose: () => { setConnected(false); toast.warning('連線中斷，嘗試重連…'); },
    onMessage: (ev) => {
      const text = typeof ev.data === 'string' ? ev.data : String(ev.data);

      // 嘗試把文字當 JSON 解；不是 JSON 就當純文字顯示  // by 瓶仔
      try {
        toast.success(text);
        const obj = JSON.parse(text);
        const content =
          (obj && typeof obj === 'object' && obj.content != null)
            ? String(obj.content)
            : text;
        if (content.trim()) {
          push({ type: 'server', content, at: Date.now() }); // by 瓶仔
        }
      } catch {
        if (text.trim()) {
          push({ type: 'server', content: text.trim(), at: Date.now() }); // by 瓶仔
        }
      }
    },
  });

  useEffect(() => {
    if (isLoggedIn) ws.connect();
    else ws.disconnect();
  }, [isLoggedIn]);

  const connect = () => ws.connect();
  const disconnect = () => { ws.disconnect(); reset(); toast.info('已斷線並回登入'); };

  // --------- 未登入：置中畫面，只顯示 LoginForm ----------
  if (!isLoggedIn) {
    return (
      <AppLayout
        statusBar={<StatusBar onConnect={connect} onDisconnect={disconnect} />}
        showLeft={false}
        showRight={false}
        center={<LoginForm />} // 直接放，交給 AppLayout 處理置中
      />
    );
  }


  // --------- 已登入：正常三欄 ----------
  return (
    <>
      <AppLayout
        statusBar={<StatusBar onConnect={connect} onDisconnect={disconnect} />}
        left={
          <Menu
            onBroadcast={() => setOpenB(true)}
            onClassroomInfo={() => setOpenB(true)}
            onComment={() => setOpenC(true)}
            onClear={() => setOpenClear(true)}
          />
        }
        center={<MessageLog />}
      />
      <BroadcastDialog
        open={openB}
        onClose={() => setOpenB(false)}
        onSubmit={(text) => {
          const ok = ws.send({
            type: 'broadcast',
            target: '',
            content: text,
          }); // by 瓶仔

          if (ok) {
            // 成功送出再記一筆自己發的訊息  by 瓶仔
            push({ type: 'broadcast', content: text, at: Date.now() });
            setOpenB(false);
            toast.success('廣播已送出'); // by 瓶仔
          } else {
            toast.error('尚未連上 Bridge / Server'); // by 瓶仔
          }
        }}
      />
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
