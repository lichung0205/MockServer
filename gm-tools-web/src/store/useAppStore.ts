import { create } from 'zustand';

export type Msg =
    | { type: 'server'; content: string; at: number } // 伺服器返回訊息
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
    setConnected: (value: boolean) => void; // 新增
    push: (m: Msg) => void;
    reset: () => void;
};

const initial: State = {
    account: '',
    nickname: '',
    isLoggedIn: false,
    connected: false,
    messages: [],
};

export const useAppStore = create<State & Actions>((set, get) => ({
    ...initial,

    login: (account, nickname) =>
        set({ account, nickname, isLoggedIn: true, connected: true }),

    toggleConnection: () => {
        const { connected } = get(); // => State
        if (connected) {
            set({ ...initial }); // 斷線 → 回初始狀態
        } else {
            set({ connected: true });
        }
    },

    setConnected: (value) => set({ connected: value }), // 設定是否連線

    push: (m) => set((s) => ({ messages: [m, ...s.messages] })), // 推訊息到MSG集合

    reset: () => set({ ...initial }),
}));

