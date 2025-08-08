// src/components/MessageLog.tsx
import { Card, CardContent, Typography, Stack } from '@mui/material';

type Props = { logs: string[] };

export default function MessageLog({ logs }: Props) {
    return (
        <Card variant="outlined" sx={{ mt: 3 }}>
            <CardContent>
                <Typography variant="h6" gutterBottom>
                    訊息紀錄
                </Typography>
                <Stack spacing={1} sx={{ maxHeight: 260, overflow: 'auto' }}>
                    {logs.length === 0 ? (
                        <Typography color="text.secondary">目前沒有訊息</Typography>
                    ) : (
                        logs.map((line, i) => (
                            <Typography key={i} variant="body2">
                                {line}
                            </Typography>
                        ))
                    )}
                </Stack>
            </CardContent>
        </Card>
    );
}
