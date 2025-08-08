// App.tsx
import { Container, Typography, Paper } from '@mui/material';
import ConnectionStatus from './components/ConnectionStatus';
import ControlPanel from './components/ControlPanel';
import MessageLog from './components/MessageLog';
import useMockWebSocket from './hooks/useMockWebSocket';

export default function App() {
  const { isConnected, logs, connect, mockSend } = useMockWebSocket();

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Paper elevation={3} sx={{ p: 3 }}>
        <Typography variant="h4" gutterBottom>
          🎓 教室管理工具
        </Typography>

        <ConnectionStatus
          isConnected={isConnected}
          onConnect={connect}
        />

        {isConnected && (
          <ControlPanel
            onAction={(type, payload) => mockSend(type, payload)}
          />
        )}

        <MessageLog logs={logs} />
      </Paper>
    </Container>
  );
}