// // components/mypage/BlockedUsersSection.tsx
// import { useEffect, useMemo, useState } from "react";
// import {
//   getBlockedUsers,
//   unblockUser,
//   type BlockedUser,
// } from "../../../api/userService";
// import { X, Undo2, Search } from "lucide-react";

// const BlockedUsersSection = () => {
//   const [list, setList] = useState<BlockedUser[]>([]);
//   const [loading, setLoading] = useState(true);
//   const [q, setQ] = useState("");
//   const [confirmUser, setConfirmUser] = useState<BlockedUser | null>(null);
//   const [error, setError] = useState<string | null>(null);

//   const refresh = async () => {
//     setLoading(true);
//     setError(null);
//     try {
//       const data = await getBlockedUsers();
//       setList(data);
//     } catch {
//       setError("차단 목록을 불러오지 못했습니다.");
//     } finally {
//       setLoading(false);
//     }
//   };

//   useEffect(() => {
//     refresh();
//   }, []);

//   const filtered = useMemo(() => {
//     const k = q.trim().toLowerCase();
//     return !k ? list : list.filter((u) => u.nickname.toLowerCase().includes(k));
//   }, [list, q]);

//   const doUnblock = async (user: BlockedUser) => {
//     // optimistic update
//     setList((prev) => prev.filter((x) => x.userId !== user.userId));
//     setConfirmUser(null);
//     try {
//       await unblockUser(user.userId);
//     } catch {
//       // rollback
//       setList((prev) => [...prev, user]);
//       alert("차단 해제에 실패했습니다. 잠시 후 다시 시도해주세요.");
//     }
//   };

//   return (
//     <section className="space-y-4">
//       <header className="flex items-center justify-between">
//         <h2 className="text-lg font-semibold">차단한 사용자</h2>
//         <div className="text-sm text-gray-400">총 {list.length}명</div>
//       </header>

//       <div className="relative">
//         <input
//           value={q}
//           onChange={(e) => setQ(e.target.value)}
//           placeholder="닉네임 검색…"
//           className="w-full bg-gray-800 border border-gray-700 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-purple-500"
//         />
//         <Search
//           size={16}
//           className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
//         />
//       </div>

//       {loading && <div className="text-sm text-gray-400">불러오는 중…</div>}

//       {error && <div className="text-sm text-red-400">{error}</div>}

//       {!loading && !error && filtered.length === 0 && (
//         <div className="text-sm text-gray-400 border border-dashed border-gray-700 rounded-lg p-6 text-center">
//           차단한 사용자가 없습니다.
//         </div>
//       )}

//       <ul className="space-y-2">
//         {filtered.map((u) => (
//           <li
//             key={u.userId}
//             className="flex items-center justify-between bg-gray-800 border border-gray-700 rounded-lg px-3 py-2"
//           >
//             <div className="flex items-center gap-3">
//               {/* 아바타 대체 */}
//               <div className="w-8 h-8 rounded-full bg-gray-700 flex items-center justify-center">
//                 <span className="text-xs">{u.nickname.charAt(0)}</span>
//               </div>
//               <div>
//                 <div className="font-medium">{u.nickname}</div>
//                 {/* {!!u.blockedAt && (
//                   <div className="text-xs text-gray-400">
//                     {new Date(u.blockedAt).toLocaleString()}
//                   </div>
//                 )} */}
//               </div>
//             </div>

//             <button
//               onClick={() => setConfirmUser(u)}
//               className="flex items-center gap-1 text-sm bg-gray-700 hover:bg-gray-600 px-3 py-1.5 rounded-md"
//               title="차단 해제"
//             >
//               <Undo2 size={14} />
//               차단 해제
//             </button>
//           </li>
//         ))}
//       </ul>

//       {/* 확인 모달 */}
//       {confirmUser && (
//         <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50">
//           <div className="bg-gray-800 border border-gray-700 rounded-xl p-5 w-full max-w-sm">
//             <div className="flex items-start justify-between">
//               <h3 className="font-semibold">차단 해제</h3>
//               <button
//                 onClick={() => setConfirmUser(null)}
//                 className="p-1 rounded hover:bg-black/20"
//               >
//                 <X size={16} />
//               </button>
//             </div>
//             <p className="text-sm text-gray-300 mt-3">
//               <b className="text-purple-300">{confirmUser.nickname}</b> 님의
//               차단을 해제할까요?
//             </p>
//             <div className="mt-5 flex justify-end gap-2">
//               <button
//                 className="px-3 py-1.5 text-sm rounded-md bg-gray-700 hover:bg-gray-600"
//                 onClick={() => setConfirmUser(null)}
//               >
//                 취소
//               </button>
//               <button
//                 className="px-3 py-1.5 text-sm rounded-md bg-purple-600 hover:bg-purple-500"
//                 onClick={() => doUnblock(confirmUser)}
//               >
//                 해제
//               </button>
//             </div>
//           </div>
//         </div>
//       )}
//     </section>
//   );
// };

// export default BlockedUsersSection;


// components/mypage/BlockedUsersSection.tsx
import { useEffect, useMemo, useState } from "react";
import {
  getBlockedUsers,
  unblockUser,
  type BlockedUser,
} from "../../../api/userService";
import { X, Undo2, Search } from "lucide-react";
import { useUserStore } from "../../../store/useUserStore";

const BlockedUsersSection = () => {
  const [list, setList] = useState<BlockedUser[]>([]);
  const [loading, setLoading] = useState(true);
  const [q, setQ] = useState("");
  const [confirmUser, setConfirmUser] = useState<BlockedUser | null>(null);
  const [error, setError] = useState<string | null>(null);

  const refresh = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getBlockedUsers();
      setList(data);
      // 전역 차단 상태 동기화
      useUserStore.getState().setBlockedList(data.map(d => d.userId));
    } catch {
      setError("차단 목록을 불러오지 못했습니다.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refresh();
  }, []);

  const filtered = useMemo(() => {
    const k = q.trim().toLowerCase();
    return !k ? list : list.filter((u) => u.nickname.toLowerCase().includes(k));
  }, [list, q]);

  const doUnblock = async (user: BlockedUser) => {
    // optimistic update
    setList((prev) => prev.filter((x) => x.userId !== user.userId));
    setConfirmUser(null);
    try {
      await unblockUser(user.userId);
      // 전역에서도 즉시 해제 반영 → 채팅 필터 즉시 풀림
      useUserStore.getState().unblockLocal(user.userId);
      // (선택) 서버와 다시 강동기화하고 싶다면:
      // await refresh();
    } catch {
      // rollback
      setList((prev) => [...prev, user]);
      alert("차단 해제에 실패했습니다. 잠시 후 다시 시도해주세요.");
    }
  };

  return (
    <section className="space-y-4">
      <header className="flex items-center justify-between">
        <h2 className="text-lg font-semibold">차단한 사용자</h2>
        <div className="text-sm text-gray-400">총 {list.length}명</div>
      </header>

      <div className="relative">
        <input
          value={q}
          onChange={(e) => setQ(e.target.value)}
          placeholder="닉네임 검색…"
          className="w-full bg-gray-800 border border-gray-700 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-purple-500"
        />
        <Search
          size={16}
          className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
        />
      </div>

      {loading && <div className="text-sm text-gray-400">불러오는 중…</div>}

      {error && <div className="text-sm text-red-400">{error}</div>}

      {!loading && !error && filtered.length === 0 && (
        <div className="text-sm text-gray-400 border border-dashed border-gray-700 rounded-lg p-6 text-center">
          차단한 사용자가 없습니다.
        </div>
      )}

      <ul className="space-y-2">
        {filtered.map((u) => (
          <li
            key={u.userId}
            className="flex items-center justify-between bg-gray-800 border border-gray-700 rounded-lg px-3 py-2"
          >
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-full bg-gray-700 flex items-center justify-center">
                <span className="text-xs">{u.nickname.charAt(0)}</span>
              </div>
              <div>
                <div className="font-medium">{u.nickname}</div>
              </div>
            </div>

            <button
              onClick={() => setConfirmUser(u)}
              className="flex items-center gap-1 text-sm bg-gray-700 hover:bg-gray-600 px-3 py-1.5 rounded-md"
              title="차단 해제"
            >
              <Undo2 size={14} />
              차단 해제
            </button>
          </li>
        ))}
      </ul>

      {/* 확인 모달 */}
      {confirmUser && (
        <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50">
          <div className="bg-gray-800 border border-gray-700 rounded-xl p-5 w-full max-w-sm">
            <div className="flex items-start justify-between">
              <h3 className="font-semibold">차단 해제</h3>
              <button
                onClick={() => setConfirmUser(null)}
                className="p-1 rounded hover:bg-black/20"
              >
                <X size={16} />
              </button>
            </div>
            <p className="text-sm text-gray-300 mt-3">
              <b className="text-purple-300">{confirmUser.nickname}</b> 님의
              차단을 해제할까요?
            </p>
            <div className="mt-5 flex justify-end gap-2">
              <button
                className="px-3 py-1.5 text-sm rounded-md bg-gray-700 hover:bg-gray-600"
                onClick={() => setConfirmUser(null)}
              >
                취소
              </button>
              <button
                className="px-3 py-1.5 text-sm rounded-md bg-purple-600 hover:bg-purple-500"
                onClick={() => doUnblock(confirmUser)}
              >
                해제
              </button>
            </div>
          </div>
        </div>
      )}
    </section>
  );
};

export default BlockedUsersSection;
