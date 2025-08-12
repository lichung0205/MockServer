import { AppBar, Toolbar, Typography, Box, Drawer } from '@mui/material';
import type { ReactNode } from 'react';

const APPBAR_H = 56;
export const APPBAR_HEIGHT = APPBAR_H;

type Props = {
    statusBar?: ReactNode;
    left?: ReactNode;
    center?: ReactNode;
    right?: ReactNode;
    showLeft?: boolean;
    showRight?: boolean;
};

export default function AppLayout({
    statusBar, left, center, right, showLeft = true, showRight = true,
}: Props) {
    return (
        <Box sx={{ display: 'flex', height: '100vh' }}>
            <AppBar position="fixed" sx={(t) => ({ zIndex: t.zIndex.drawer + 1 })}>
                <Toolbar sx={{ minHeight: APPBAR_H }}>
                    <Typography sx={{ flex: 1 }}>🎓 教室管理工具</Typography>
                    {statusBar}
                </Toolbar>
            </AppBar>

            {showLeft && (
                <Drawer
                    variant="permanent"
                    sx={{
                        width: 280,
                        flexShrink: 0,
                        '& .MuiDrawer-paper': {
                            width: 280,
                            top: APPBAR_H,
                            p: 2,
                            boxSizing: 'border-box',
                        },
                    }}
                >
                    {left}
                </Drawer>
            )}

            {/* 修正的中間區域 */}
            <Box
                sx={{
                    position: 'fixed',
                    inset: `${APPBAR_H}px 0 0 0`, // top right bottom left
                    display: 'grid',
                    placeItems: 'center',
                    // 可選：背景色
                    // bgcolor: '#f5f5f5'
                }}
            >
                {center}
            </Box>


            {showRight && (
                <Box
                    sx={{
                        width: 320,
                        position: 'fixed',
                        right: 0,
                        top: APPBAR_H,
                        height: `calc(100vh - ${APPBAR_H}px)`,
                        p: 2,
                        borderLeft: 1,
                        borderColor: 'divider',
                        overflow: 'auto',
                        backgroundColor: 'background.paper',
                    }}
                >
                    {right}
                </Box>
            )}
        </Box>
    );
}