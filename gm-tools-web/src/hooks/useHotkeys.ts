import { useEffect } from 'react';

export const useHotkeys = (map: Record<string, () => void>) => {
    useEffect(() => {
        const handler = (e: KeyboardEvent) => {
            if (e.ctrlKey && e.key === 'Enter' && map['Ctrl+Enter']) {
                e.preventDefault();
                map['Ctrl+Enter']();
            }
            if (e.key === '/' && map['/']) {
                e.preventDefault();
                map['/']();
            }
            if (e.key.toLowerCase() === 'r' && map['R']) {
                e.preventDefault();
                map['R']();
            }
        };

        window.addEventListener('keydown', handler);
        return () => window.removeEventListener('keydown', handler);
    }, [map]);
};
