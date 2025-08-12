import { Card, CardContent, Stack, Typography, Box } from '@mui/material';
import { useAppStore } from '@/store/useAppStore';


export default function MessageLog() {
    const messages = useAppStore(s => s.messages);

    return (
        <Card>
            <CardContent>
                <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                    與 Server 互動訊息紀錄（目前為本機 Mock）
                </Typography>
                <Stack spacing={1}>
                    {messages.length === 0 && <Typography color="text.secondary">目前沒有訊息</Typography>}
                    {messages.map((m, i) => (
                        <Box key={i} sx={{ fontFamily: 'monospace', fontSize: 14 }}>
                            {new Date(m.at).toLocaleTimeString()} —{' '}
                            {m.type === 'server' && `${m.content ?? ''}`}{/* ← 新增：顯示 Server 回傳 */}
                            {m.type === 'broadcast' && `【廣播】${m.content}`}
                            {m.type === 'comment' && `【留言】(${m.studentId}) ${m.content}`}
                            {m.type === 'clear' && `【清場】${m.confirm ? '已執行' : '已取消'}`}
                        </Box>
                    ))}
                </Stack>
            </CardContent>
        </Card>
    );
}
