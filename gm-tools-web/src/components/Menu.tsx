import { List, ListItemButton, ListItemText, Typography } from '@mui/material';
import { useAppStore } from '@/store/useAppStore';

type Props = {
    onBroadcast: () => void;
    onComment: () => void;
    onClear: () => void;
};

export default function Menu({ onBroadcast, onComment, onClear }: Props) {
    const isLoggedIn = useAppStore(s => s.isLoggedIn);
    if (!isLoggedIn) return <Typography color="text.secondary">請先登入</Typography>;

    return (
        <List dense>
            <ListItemButton onClick={onBroadcast}><ListItemText primary="廣播" /></ListItemButton>
            <ListItemButton onClick={onComment}><ListItemText primary="留言" /></ListItemButton>
            <ListItemButton onClick={onClear}><ListItemText primary="清場" /></ListItemButton>
        </List>
    );
}
