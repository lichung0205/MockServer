import { AppBar, Toolbar, Typography, Box, Drawer } from '@mui/material';
import type { ReactNode } from 'react';

const APPBAR_H = 56;
export const APPBAR_HEIGHT = APPBAR_H;

type Props = { left?: ReactNode; center?: ReactNode; right?: ReactNode; statusBar?: ReactNode; };

export default function AppLayout({ statusBar, left, center, right }: Props) {
    return (
        <Box sx={{ display: 'flex', height: '100vh' }}>
            <AppBar position="fixed" sx={t => ({ zIndex: t.zIndex.drawer + 1 })}>
                <Toolbar sx={{ minHeight: APPBAR_H }}>
                    <Typography sx={{ flex: 1 }}>🎓 教室管理工具</Typography>
                    {statusBar}
                </Toolbar>
            </AppBar>

            <Drawer variant="permanent"
                sx={{ width: 280, '& .MuiDrawer-paper': { width: 280, top: APPBAR_H, p: 2 } }}>
                {left}
            </Drawer>

            <Box sx={{ flex: 1, pt: `${APPBAR_H}px`, p: 2, overflow: 'auto' }}>
                {center}
            </Box>

            <Box sx={{ width: 320, pt: `${APPBAR_H}px`, p: 2, borderLeft: 1, borderColor: 'divider', overflow: 'auto' }}>
                {right}
            </Box>
        </Box>
    );
}
