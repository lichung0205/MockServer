import { Card, CardContent, Stack, TextField, Typography, Button } from '@mui/material';
import { useState } from 'react';
import { useAppStore } from '@/store/useAppStore';

export default function LoginForm() {
    const login = useAppStore(s => s.login);
    const [account, setAccount] = useState('');
    const [nickname, setNickname] = useState('');

    return (
        <Card sx={{ maxWidth: 420 }}>
            <CardContent>
                <Typography variant="h6" gutterBottom>登入</Typography>
                <Stack spacing={2}>
                    <TextField label="帳號" value={account} onChange={e => setAccount(e.target.value)} autoFocus />
                    <TextField label="暱稱" value={nickname} onChange={e => setNickname(e.target.value)} />
                    <Button variant="contained" onClick={() => account && nickname && login(account, nickname)}>
                        登入
                    </Button>
                </Stack>
            </CardContent>
        </Card>
    );
}
