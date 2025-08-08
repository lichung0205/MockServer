// src/components/ConnectionStatus.tsx
import { Stack, Button, Alert } from '@mui/material';

type Props = {
    isConnected: boolean;
    onConnect: () => void;
};

export default function ConnectionStatus({ isConnected, onConnect }: Props) {
    return (
        <Stack direction="row" spacing={2} alignItems="center">
            <Alert severity={isConnected ? 'success' : 'warning'}>
                {isConnected ? '已連線（Mock）' : '尚未連線'}
            </Alert>
            {!isConnected && (
                <Button variant="contained" onClick={onConnect}>
                    連線
                </Button>
            )}
        </Stack>
    );
}
