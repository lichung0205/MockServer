import { Button, Chip, Stack } from '@mui/material';
import { useAppStore } from '@/store/useAppStore';

type Props = {
    onConnect: () => void;
    onDisconnect: () => void;
};

export default function StatusBar({ onConnect, onDisconnect }: Props) {
    const connected = useAppStore(s => s.connected);
    const isLoggedIn = useAppStore(s => s.isLoggedIn);

    const acc = useAppStore(s => s.account);
    const name = useAppStore(s => s.nickname);

    return (
        <Stack direction="row" spacing={1} alignItems="center">
            <Chip
                label={
                    connected ? (
                        <span>
                            <strong>{acc} - {name}</strong> 已連線 (Mock) 
                        </span>
                    ) : (
                        '尚未連線'
                    )
                }
                color={connected ? 'success' : 'error'}
                variant={connected ? 'filled' : 'outlined'}
                size="small"
            />
            {isLoggedIn && (
                <Button
                    size="small"
                    variant="contained"
                    onClick={connected ? onDisconnect : onConnect}
                >
                    {connected ? '斷線' : '連線'}
                </Button>
            )}
        </Stack>
    );
}
