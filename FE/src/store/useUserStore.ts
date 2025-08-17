import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { User } from '../types';
import { emitTokenRefreshed } from '../api/axiosInstance';

type UserState = {
  myUser: User | null // 내 정보
  otherUser: User | null  // 타 유저 정보
  setMyUser: (user: User | null) => void
  setOtherUser: (user: User | null) => void
};

export const useUserStore = create<UserState>()(
  persist(
    (set) => ({
      myUser: null,
      otherUser: null,
      setMyUser: (user) => set({ myUser: user }),
      setOtherUser: (user) => set({ otherUser: user }),
      logout: () => {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        emitTokenRefreshed(null); // 토큰 null 브로드캐스트
        set({ myUser: null, otherUser: null });
      },
    }),
    {
      name: 'user-storage',
      // storage: createJSONStorage(() => localStorage),
    }
  )
);
