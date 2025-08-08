// 通用吐司

import { createContext, useContext, useState, type ReactNode } from 'react';
import { Snackbar, Alert, type AlertColor } from '@mui/material';

type ToastFn = (msg: string, severity?: AlertColor) => void;
const ToastCtx = createContext<{ show: ToastFn } | null>(null);

export function ToastProvider({ children }: { children: ReactNode }) {
    const [open, setOpen] = useState(false);
    const [msg, setMsg] = useState('');
    const [severity, setSeverity] = useState<AlertColor>('info');

    const show: ToastFn = (m, s = 'info') => { setMsg(m); setSeverity(s); setOpen(true); };

    return (
        <ToastCtx.Provider value={{ show }}>
            {children}
            <Snackbar open={open} autoHideDuration={3000} onClose={() => setOpen(false)}>
                <Alert variant="filled" onClose={() => setOpen(false)} severity={severity} sx={{ width: '100%' }}>
                    {msg}
                </Alert>
            </Snackbar>
        </ToastCtx.Provider>
    );
}

export function useToast() {
    const ctx = useContext(ToastCtx);
    if (!ctx) throw new Error('useToast must be used within <ToastProvider>');
    return {
        info: (m: string) => ctx.show(m, 'info'),
        success: (m: string) => ctx.show(m, 'success'),
        warning: (m: string) => ctx.show(m, 'warning'),
        error: (m: string) => ctx.show(m, 'error'),
    };
}
