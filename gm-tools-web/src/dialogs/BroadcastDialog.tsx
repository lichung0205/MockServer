// 廣播UI
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField } from '@mui/material';
import { useState } from 'react';
import { useAppStore } from '@/store/useAppStore';

export default function BroadcastDialog({ open, onClose }: { open: boolean; onClose: () => void; }) {
    const push = useAppStore(s => s.push);
    const [text, setText] = useState('');
    const submit = () => {
        if (!text.trim()) return;
        push({ type: 'broadcast', content: text.trim(), at: Date.now() });
        setText(''); onClose();
    };

    return (
        <Dialog open={open} onClose={onClose} fullWidth maxWidth="xs">
            <DialogTitle>發送廣播</DialogTitle>
            <DialogContent>
                <TextField label="內容" fullWidth multiline minRows={3} value={text} onChange={e => setText(e.target.value)} />
            </DialogContent>
            <DialogActions>
                <Button onClick={onClose}>取消</Button>
                <Button variant="contained" onClick={submit}>送出</Button>
            </DialogActions>
        </Dialog>
    );
}
