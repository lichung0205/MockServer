import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Typography } from '@mui/material';

type Props = {
    open: boolean; title?: string; message: string;
    confirmText?: string; cancelText?: string; confirmColor?: 'primary' | 'error' | 'inherit' | 'secondary' | 'success' | 'info' | 'warning';
    onClose: () => void; onConfirm: () => void;
};

export default function ConfirmDialog({
    open, title = '確認', message, confirmText = '確認', cancelText = '取消',
    confirmColor = 'error', onClose, onConfirm
}: Props) {
    return (
        <Dialog open={open} onClose={onClose} fullWidth maxWidth="xs">
            <DialogTitle>{title}</DialogTitle>
            <DialogContent><Typography>{message}</Typography></DialogContent>
            <DialogActions>
                <Button onClick={onClose}>{cancelText}</Button>
                <Button variant="contained" color={confirmColor} onClick={onConfirm}>{confirmText}</Button>
            </DialogActions>
        </Dialog>
    );
}
