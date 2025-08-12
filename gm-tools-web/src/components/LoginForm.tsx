import { Card, CardContent, Stack, TextField, Typography, Button } from '@mui/material';
import { useState } from 'react';
import { useAppStore } from '@/store/useAppStore';
import { useToast } from '@/providers/ToastProvider';

export default function LoginForm() {
    const login = useAppStore(s => s.login);
    const toast = useToast();
    const [account, setAccount] = useState('');
    const [nickname, setNickname] = useState('');
    const [touched, setTouched] = useState(false);

    const submit = () => {
        setTouched(true);
        if (!account.trim() || !nickname.trim()) {
            toast.error('請輸入帳號與暱稱');
            return;
        }
        login(account.trim(), nickname.trim());
    };

    const acctErr = touched && !account.trim();
    const nickErr = touched && !nickname.trim();

    return (
        <Card
            sx={{
                // width: 1024,
                // width: '100%', // 強制全寬
                // maxWidth: '90vw', // 確保在小螢幕上不會超出
                borderRadius: 3,
                boxShadow: 3,
                margin: 'auto' // 額外保險
            }}
        >
            <CardContent>
                <Typography variant="h6" gutterBottom>登入</Typography>
                <Stack spacing={2}>
                    <TextField
                        label="帳號"
                        value={account}
                        onChange={e => setAccount(e.target.value)}
                        error={acctErr}
                        helperText={acctErr ? '必填' : ' '}
                        autoFocus
                    />
                    <TextField
                        label="暱稱"
                        value={nickname}
                        onChange={e => setNickname(e.target.value)}
                        error={nickErr}
                        helperText={nickErr ? '必填' : ' '}
                    />
                    <Button variant="contained" onClick={submit}>登入</Button>
                </Stack>
            </CardContent>
        </Card>
    );
}