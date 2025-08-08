import { useState } from 'react';
import AppLayout from '@/components/layout/AppLayout';
import StatusBar from '@/components/StatusBar';
import Menu from '@/components/Menu';
import MessageLog from '@/components/MessageLog';
import LoginForm from '@/components/LoginForm';
import BroadcastDialog from '@/dialogs/BroadcastDialog';
import CommentDialog from '@/dialogs/CommentDialog';
import ConfirmDialog from '@/dialogs/ConfirmDialog';
import { useAppStore } from '@/store/useAppStore';

export default function App() {
  const isLoggedIn = useAppStore(s => s.isLoggedIn);
  const push = useAppStore(s => s.push);

  const [openB, setOpenB] = useState(false);
  const [openC, setOpenC] = useState(false);
  const [openClear, setOpenClear] = useState(false);

  return (
    <>
      <AppLayout
        statusBar={<StatusBar />}
        left={
          <Menu
            onBroadcast={() => setOpenB(true)}
            onComment={() => setOpenC(true)}
            onClear={() => setOpenClear(true)}
          />
        }
        center={isLoggedIn ? <MessageLog /> : <LoginForm />}
      />

      {/* dialogs */}
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
