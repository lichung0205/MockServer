import { create } from 'zustand';

export type Msg =
    | { type: 'broadcast'; content: string; at: number }
    | { type: 'comment'; studentId: string; content: string; at: number }
    | { type: 'clear'; at: number; confirm: boolean };

type State = {
    account: string;
    nickname: string;
    isLoggedIn: boolean;
    connected: boolean;
    messages: Msg[];
};

type Actions = {
    login: (account: string, nickname: string) => void;
    toggleConnection: () => void;
    push: (m: Msg) => void;
    reset: () => void;
};

const initial: State = {
    account: '', nickname: '',
    isLoggedIn: false, connected: false,
    messages: [],
};

export const useAppStore = create<State & Actions>((set, get) => ({
    ...initial,
    login: (account, nickname) => set({ account, nickname, isLoggedIn: true, connected: true }),
    toggleConnection: () => {
        const { connected } = get();
        if (connected) {
            // 斷線 = 回初始狀態
            set({ ...initial });
        } else {
            set({ connected: true });
        }
    },
    push: (m) => set(s => ({ messages: [m, ...s.messages] })),
    reset: () => set({ ...initial }),
}));
