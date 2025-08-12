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
                // color={connected ? 'success' : 'error'}
                // 讓藍底上可讀：白底＋彩色字，並加一點白色描邊
                sx={{
                    bgcolor: 'common.white',
                    color: connected ? 'success.main' : 'error.main',
                    border: '1px solid rgba(255,255,255,0.55)',
                    fontWeight: 600,
                    '& .MuiChip-label': { px: 1 } // 讓文字不擠
                }}
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
