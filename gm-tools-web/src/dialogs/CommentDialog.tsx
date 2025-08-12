// 留言UI
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Stack } from '@mui/material';
import { useState } from 'react';
import { useAppStore } from '@/store/useAppStore';

export default function CommentDialog({ open, onClose,
    onSubmit // by 瓶仔
}: {
    open: boolean; onClose: () => void;
    onSubmit: (id: string, text: string) => void; // by 瓶仔
}) {
    const push = useAppStore(s => s.push);
    const [sid, setSid] = useState('');
    const [text, setText] = useState('');

    const submit = () => {
        if (!sid.trim() || !text.trim()) return;
        push({ type: 'comment', studentId: sid.trim(), content: text.trim(), at: Date.now() });
        setSid(''); setText(''); onClose();
    };

    return (
        <Dialog open={open} onClose={onClose} fullWidth maxWidth="xs">
            <DialogTitle>留言</DialogTitle>
            <DialogContent>
                <Stack spacing={2} sx={{ mt: 1 }}>
                    <TextField label="學員代號" value={sid} onChange={e => setSid(e.target.value)} />
                    <TextField label="內容" multiline minRows={2} value={text} onChange={e => setText(e.target.value)} />
                </Stack>
            </DialogContent>
            <DialogActions>
                <Button onClick={onClose}>取消</Button>
                <Button variant="contained" onClick={submit}>送出</Button>
            </DialogActions>
        </Dialog>
    );
}
