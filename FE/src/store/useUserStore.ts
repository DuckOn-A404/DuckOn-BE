// import { create } from 'zustand';
// import { persist } from 'zustand/middleware';
// import type { User } from '../types';
// import { emitTokenRefreshed } from '../api/axiosInstance';

// type UserState = {
//   myUser: User | null // 내 정보
//   otherUser: User | null  // 타 유저 정보
//   setMyUser: (user: User | null) => void
//   setOtherUser: (user: User | null) => void
// };

// export const useUserStore = create<UserState>()(
//   persist(
//     (set) => ({
//       myUser: null,
//       otherUser: null,
//       setMyUser: (user) => set({ myUser: user }),
//       setOtherUser: (user) => set({ otherUser: user }),
//       logout: () => {
//         localStorage.removeItem("accessToken");
//         localStorage.removeItem("refreshToken");
//         emitTokenRefreshed(null); // 토큰 null 브로드캐스트
//         set({ myUser: null, otherUser: null });
//       },
//     }),
//     {
//       name: 'user-storage',
//       // storage: createJSONStorage(() => localStorage),
//     }
//   )
// );



// // store/useUserStore.ts
// import { create } from 'zustand';
// import { persist } from 'zustand/middleware';
// import type { User } from '../types';
// import { emitTokenRefreshed } from '../api/axiosInstance';

// type UserState = {
//   myUser: User | null;           // 내 정보
//   otherUser: User | null;        // 타 유저 정보
//   setMyUser: (user: User | null) => void;
//   setOtherUser: (user: User | null) => void;

//   // --- 추가: 전역 차단 상태 ---
//   blockedSet: Set<string>;
//   setBlockedList: (ids: string[]) => void;
//   blockLocal: (userId: string) => void;
//   unblockLocal: (userId: string) => void;

//   logout: () => void;
// };

// export const useUserStore = create<UserState>()(
//   persist(
//     (set) => ({
//       myUser: null,
//       otherUser: null,
//       setMyUser: (user) => set({ myUser: user }),
//       setOtherUser: (user) => set({ otherUser: user }),

//       // --- 전역 차단 상태 초기값 ---
//       blockedSet: new Set<string>(),
//       setBlockedList: (ids) => set({ blockedSet: new Set(ids) }),
//       blockLocal: (userId) =>
//         set((s) => {
//           const ns = new Set(s.blockedSet);
//           ns.add(userId);
//           return { blockedSet: ns };
//         }),
//       unblockLocal: (userId) =>
//         set((s) => {
//           const ns = new Set(s.blockedSet);
//           ns.delete(userId);
//           return { blockedSet: ns };
//         }),

//       logout: () => {
//         localStorage.removeItem("accessToken");
//         localStorage.removeItem("refreshToken");
//         emitTokenRefreshed(null); // 토큰 null 브로드캐스트
//         set({ myUser: null, otherUser: null, blockedSet: new Set() });
//       },
//     }),
//     {
//       name: 'user-storage',
//     }
//   )
// );


// // store/useUserStore.ts
// import { create } from 'zustand';
// import { persist } from 'zustand/middleware';
// import type { User } from '../types';
// import { emitTokenRefreshed } from '../api/axiosInstance';

// // 공용 유틸
// const isEmpty = (v: unknown) =>
//   v === undefined || v === null || (typeof v === 'string' && v.trim() === '');

// const lastImgKey = (userId: string) => `last-img:${userId}`;
// const saveLastImg = (userId?: string, url?: string) => {
//   if (!userId) return;
//   if (!isEmpty(url)) localStorage.setItem(lastImgKey(userId), String(url));
// };
// const loadLastImg = (userId?: string): string | undefined => {
//   if (!userId) return undefined;
//   const v = localStorage.getItem(lastImgKey(userId)) || undefined;
//   return v && v.trim() !== '' ? v : undefined;
// };

// type UserState = {
//   myUser: User | null;           // 내 정보
//   otherUser: User | null;        // 타 유저 정보
//   setMyUser: (user: User | null) => void;
//   setOtherUser: (user: User | null) => void;

//   // --- 전역 차단 상태 ---
//   blockedSet: Set<string>;
//   setBlockedList: (ids: string[]) => void;
//   blockLocal: (userId: string) => void;
//   unblockLocal: (userId: string) => void;

//   logout: () => void;
// };

// export const useUserStore = create<UserState>()(
//   persist(
//     (set) => ({
//       myUser: null,
//       otherUser: null,

//       // 어디서든 setMyUser가 호출될 때 이미지 보존/복구
//       setMyUser: (user) =>
//         set((state) => {
//           if (user == null) return { myUser: null };

//           const prev = state.myUser as any;
//           const uid = (user as any).userId ?? prev?.userId;
//           const incomingImg = (user as any).imgUrl;

//           // 1) 서버가 유효한 imgUrl 주면 그대로 사용 + 캐시
//           // 2) 비어있으면 이전값 또는 last-img 캐시로 복구
//           const fallbackImg = prev?.imgUrl ?? loadLastImg(uid);
//           const nextImg = !isEmpty(incomingImg) ? incomingImg : fallbackImg;

//           const next = { ...prev, ...user, imgUrl: nextImg } as any;

//           // 마지막으로 확인된 유효 이미지 캐시 저장 (로그아웃 후 복구용)
//           if (!isEmpty(nextImg)) saveLastImg(uid, nextImg);

//           return { myUser: next };
//         }),

//       setOtherUser: (user) => set({ otherUser: user }),

//       // --- 전역 차단 상태 ---
//       blockedSet: new Set<string>(),
//       setBlockedList: (ids) => set({ blockedSet: new Set(ids) }),
//       blockLocal: (userId) =>
//         set((s) => {
//           const ns = new Set(s.blockedSet);
//           ns.add(userId);
//           return { blockedSet: ns };
//         }),
//       unblockLocal: (userId) =>
//         set((s) => {
//           const ns = new Set(s.blockedSet);
//           ns.delete(userId);
//           return { blockedSet: ns };
//         }),

//       logout: () => {
//         localStorage.removeItem("accessToken");
//         localStorage.removeItem("refreshToken");
//         emitTokenRefreshed(null);
//         // last-img 캐시는 지우지 않습니다 (다음 로그인 복구를 위해)
//         set({ myUser: null, otherUser: null, blockedSet: new Set() });
//       },
//     }),
//     { name: 'user-storage' }
//   )
// );


// store/useUserStore.ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { User } from '../types';
import { emitTokenRefreshed } from '../api/axiosInstance';

// 공용 유틸 (이미지 보존용)
const isEmpty = (v: unknown) =>
  v === undefined || v === null || (typeof v === 'string' && v.trim() === '');

const lastImgKey = (userId: string) => `last-img:${userId}`;
const saveLastImg = (userId?: string, url?: string) => {
  if (!userId) return;
  if (!isEmpty(url)) localStorage.setItem(lastImgKey(userId), String(url));
};
const loadLastImg = (userId?: string): string | undefined => {
  if (!userId) return undefined;
  const v = localStorage.getItem(lastImgKey(userId)) || undefined;
  return v && v.trim() !== '' ? v : undefined;
};

type UserState = {
  myUser: User | null;           // 내 정보
  otherUser: User | null;        // 타 유저 정보
  setMyUser: (user: User | null) => void;
  setOtherUser: (user: User | null) => void;

  // 전역 차단 상태
  blockedSet: Set<string>;
  setBlockedList: (ids: string[]) => void;
  blockLocal: (userId: string) => void;
  unblockLocal: (userId: string) => void;

  logout: () => void;
};

export const useUserStore = create<UserState>()(
  persist(
    (set) => ({
      myUser: null,
      otherUser: null,

      // 어디서든 setMyUser 호출 시 이미지 보존/복구
      setMyUser: (user) =>
        set((state) => {
          if (user == null) return { myUser: null };

          const prev = state.myUser as any;
          const uid = (user as any).userId ?? prev?.userId;
          const incomingImg = (user as any).imgUrl;

          // 유효한 imgUrl이 오면 그대로, 아니면 이전/캐시 이미지로 복구
          const fallbackImg = prev?.imgUrl ?? loadLastImg(uid);
          const nextImg = !isEmpty(incomingImg) ? incomingImg : fallbackImg;

          const next = { ...prev, ...user, imgUrl: nextImg } as any;

          if (!isEmpty(nextImg)) saveLastImg(uid, nextImg);

          return { myUser: next };
        }),

      setOtherUser: (user) => set({ otherUser: user }),

      // 전역 차단 상태 (항상 Set 유지)
      blockedSet: new Set<string>(),
      setBlockedList: (ids) => set({ blockedSet: new Set(ids) }),
      blockLocal: (userId) =>
        set((s) => {
          const ns = new Set(s.blockedSet);
          ns.add(userId);
          return { blockedSet: ns };
        }),
      unblockLocal: (userId) =>
        set((s) => {
          const ns = new Set(s.blockedSet);
          ns.delete(userId);
          return { blockedSet: ns };
        }),

      logout: () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        emitTokenRefreshed(null);
        // last-img 캐시는 남겨둠(다음 로그인 복원용)
        set({ myUser: null, otherUser: null, blockedSet: new Set() });
      },
    }),
    {
      name: 'user-storage',
      // 퍼시스트 복원 시 blockedSet을 다시 Set으로 강제 변환
      onRehydrateStorage: () => (state) => {
        try {
          const raw: any = (state as any)?.blockedSet;
          if (!(raw instanceof Set)) {
            // raw가 배열이면 그대로, 객체({})면 빈 배열로
            const ids = Array.isArray(raw) ? raw : raw ? Object.values(raw) : [];
            // set은 상위 클로저의 set
            // @ts-ignore
            useUserStore.setState({ blockedSet: new Set(ids as string[]) });
          }
        } catch {
          // noop
        }
      },
    }
  )
);
