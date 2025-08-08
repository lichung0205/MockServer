import { Button, Chip, Stack } from '@mui/material';
import { useAppStore } from '@/store/useAppStore';


export default function StatusBar() {
    const connected = useAppStore(s => s.connected);
    const isLoggedIn = useAppStore(s => s.isLoggedIn);
    const toggleConnection = useAppStore(s => s.toggleConnection);

    return (
        <Stack direction="row" spacing={1} alignItems="center">
            <Chip
                label={connected ? '已連線（Mock）' : '尚未連線'}
                color={connected ? 'success' : 'default'}
                variant={connected ? 'filled' : 'outlined'}
                size="small"
            />
            {isLoggedIn && (
                <Button size="small" variant="contained" onClick={toggleConnection}>
                    {connected ? '斷線' : '連線'}
                </Button>
            )}
        </Stack>
    );
}
