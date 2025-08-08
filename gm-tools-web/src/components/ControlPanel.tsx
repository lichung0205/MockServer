// src/components/ControlPanel.tsx
import { useState } from 'react';
import { Button, TextField, Stack, Card, CardContent } from '@mui/material';

export default function ControlPanel({ onAction }: {
  onAction: (type: string, payload?: any) => void
}) {
  const [message, setMessage] = useState('');
  const [targetUser, setTargetUser] = useState('');

  return (
    <Card variant="outlined" sx={{ mt: 3 }}>
      <CardContent>
        <Stack spacing={2}>
          <TextField
            label="廣播訊息"
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            fullWidth
          />
          <Button
            variant="contained"
            onClick={() => onAction('broadcast', { message })}
          >
            發送廣播
          </Button>

          <TextField
            label="學員名稱"
            value={targetUser}
            onChange={(e) => setTargetUser(e.target.value)}
            fullWidth
          />
          <Button
            variant="outlined"
            onClick={() => onAction('find', { target: targetUser })}
          >
            呼叫學員
          </Button>

          <Button onClick={() => onAction('count')}>
            統計人數
          </Button>
        </Stack>
      </CardContent>
    </Card>
  );
}
