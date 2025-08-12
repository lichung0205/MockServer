import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField } from '@mui/material';
import { useState } from 'react';

export default function BroadcastDialog({
    open,
    onClose,
    onSubmit, // by 瓶仔
}: {
    open: boolean;
    onClose: () => void;
    onSubmit: (text: string) => void; // by 瓶仔
}) {
    const [text, setText] = useState('');

    const submit = () => {
        const t = text.trim();
        if (!t) return;
        onSubmit(t);            // 呼叫父層傳進來的送出方法  by 瓶仔
        setText('');            // 清空輸入框  by 瓶仔
    };

    return (
        <Dialog open={open} onClose={onClose} fullWidth maxWidth="xs">
            <DialogTitle>發送廣播</DialogTitle>
            <DialogContent>
                <TextField
                    label="內容"
                    fullWidth
                    multiline
                    minRows={3}
                    value={text}
                    onChange={e => setText(e.target.value)}
                    onKeyDown={(e) => { if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) submit(); }} // Ctrl/Cmd+Enter 快速送出  by 瓶仔
                />
            </DialogContent>
            <DialogActions>
                <Button onClick={onClose}>取消</Button>
                <Button variant="contained" onClick={submit}>送出</Button>
            </DialogActions>
        </Dialog>
    );
}
