// import { useEffect, useState, type RefObject } from "react";
// import type { artistChatMessage } from "../../types/artistChat";
// import { useUserStore } from "../../store/useUserStore";
// import { Popover } from "@headlessui/react";
// import { MoreVertical, UserX } from "lucide-react";
// import { blockUser } from "../../api/userService";
// import ConfirmModal from "../../components/common/modal/ConfirmModal";

// type ChatTabProps = {
//   messages: artistChatMessage[];
//   scrollContainerRef: RefObject<HTMLDivElement | null>;
// };

// const ArtistChatTab = ({ messages, scrollContainerRef }: ChatTabProps) => {
//   const { myUser } = useUserStore();

//   const [modalInfo, setModalInfo] = useState({
//     isOpen: false,
//     title: "",
//     description: "",
//   });

//   useEffect(() => {
//     if (scrollContainerRef.current) {
//       scrollContainerRef.current.scrollTo({
//         top: scrollContainerRef.current.scrollHeight,
//         behavior: "smooth",
//       });
//     }
//   }, [messages, scrollContainerRef]);

//   // 차단 버튼 핸들러
//   const handleBlock = async (userId: string, userNickname: string) => {
//     try {
//       await blockUser(userId);
//       // 성공 시 모달 상태 설정
//       setModalInfo({
//         isOpen: true,
//         title: "차단 완료",
//         description: `${userNickname}님을 차단했습니다.`,
//       });
//     } catch {
//       // 실패 시 모달 상태 설정
//       setModalInfo({
//         isOpen: true,
//         title: "오류",
//         description: "차단 요청에 실패했습니다. 다시 시도해주세요.",
//       });
//     }
//   };

//   if (!Array.isArray(messages)) {
//     return (
//       <div className="p-4 text-sm text-gray-500">채팅을 불러오는 중...</div>
//     );
//   }

//   if (messages.length === 0) {
//     return (
//       <div className="flex items-center justify-center h-full">
//         <p className="text-sm text-gray-400">아직 채팅 메시지가 없습니다.</p>
//       </div>
//     );
//   }

//   return (
//     <>
//       <div className="space-y-4 p-1">
//         {messages.map((msg) => (
//           <div
//             key={msg.messageId}
//             className={`flex items-start gap-3 ${
//               msg.userId === myUser?.userId ? "justify-end" : "justify-start"
//             }`}
//           >
//             <div
//               className={`flex flex-col ${
//                 msg.userId === myUser?.userId ? "items-end" : "items-start"
//               }`}
//             >
//               <span className="text-sm font-semibold text-gray-700 mb-1">
//                 {msg.userNickname}
//               </span>
//               <div className="flex items-end gap-2">
//                 {msg.userId === myUser?.userId ? (
//                   // 내가 보낸 메시지
//                   <>
//                     <span className="text-xs text-gray-400 self-end">
//                       {new Date(msg.sentAt).toLocaleTimeString(undefined, {
//                         hour: "2-digit",
//                         minute: "2-digit",
//                       })}
//                     </span>
//                     <div className="relative group px-4 py-2 rounded-xl max-w-xs break-words bg-purple-600 text-white rounded-br-none">
//                       <span>{msg.content}</span>
//                     </div>
//                   </>
//                 ) : (
//                   // 남이 보낸 메시지
//                   <>
//                     <div className="relative group px-4 py-2 rounded-xl max-w-xs break-words bg-gray-200 text-gray-800 rounded-bl-none">
//                       <span>{msg.content}</span>
//                       <Popover className="absolute top-0 right-0">
//                         <Popover.Button className="p-1 rounded-full text-gray-500 opacity-0 group-hover:opacity-100 transition-opacity focus:outline-none focus:opacity-100 hover:bg-black/10">
//                           <MoreVertical size={16} />
//                         </Popover.Button>
//                         <Popover.Panel className="absolute z-10 top-0 left-full ml-1 w-32 bg-white border rounded-lg shadow-lg">
//                           <div className="p-1">
//                             <button
//                               onClick={() =>
//                                 handleBlock(msg.userId, msg.userNickname)
//                               }
//                               className="flex items-center gap-2 w-full px-3 py-1.5 text-sm text-left text-gray-700 hover:bg-gray-100 rounded-md"
//                             >
//                               <UserX size={14} />
//                               <span>차단하기</span>
//                             </button>
//                           </div>
//                         </Popover.Panel>
//                       </Popover>
//                     </div>
//                     <span className="text-xs text-gray-400 self-end">
//                       {new Date(msg.sentAt).toLocaleTimeString(undefined, {
//                         hour: "2-digit",
//                         minute: "2-digit",
//                       })}
//                     </span>
//                   </>
//                 )}
//               </div>
//             </div>
//           </div>
//         ))}
//       </div>

//       {/* 5. ConfirmModal 컴포넌트를 렌더링하고 상태와 핸들러를 연결합니다. */}
//       <ConfirmModal
//         isOpen={modalInfo.isOpen}
//         title={modalInfo.title}
//         description={modalInfo.description}
//         confirmText="확인"
//         // 알림 모달이므로 확인/취소 버튼 모두 모달을 닫는 역할을 하도록 설정합니다.
//         onConfirm={() => setModalInfo({ ...modalInfo, isOpen: false })}
//         onCancel={() => setModalInfo({ ...modalInfo, isOpen: false })}
//       />
//     </>
//   );
// };

// export default ArtistChatTab;


// components/.../ArtistChatTab.tsx
import { useEffect, useMemo, useState, type RefObject } from "react";
import type { artistChatMessage } from "../../types/artistChat";
import { useUserStore } from "../../store/useUserStore";
import { Popover } from "@headlessui/react";
import { MoreVertical, UserX } from "lucide-react";
import { blockUser, getBlockedUsers } from "../../api/userService";
import ConfirmModal from "../../components/common/modal/ConfirmModal";

type ChatTabProps = {
  messages: artistChatMessage[];
  scrollContainerRef: RefObject<HTMLDivElement | null>;
};

const ArtistChatTab = ({ messages, scrollContainerRef }: ChatTabProps) => {
  const { myUser } = useUserStore();
  const blockedSet = useUserStore((s) => s.blockedSet);
  const setBlockedList = useUserStore((s) => s.setBlockedList);
  const blockLocal = useUserStore((s) => s.blockLocal);

  const [modalInfo, setModalInfo] = useState({
    isOpen: false,
    title: "",
    description: "",
  });

  // 초기 동기화 (비어있으면 한 번만)
  useEffect(() => {
    (async () => {
      if (!myUser) return;
      if (blockedSet.size > 0) return;
      try {
        const list = await getBlockedUsers();
        setBlockedList(list.map((d) => d.userId));
      } catch {
        // ignore
      }
    })();
  }, [myUser, blockedSet.size, setBlockedList]);

  // 차단 기준으로 필터링
  const visibleMessages = useMemo(() => {
    if (!Array.isArray(messages)) return [];
    return messages.filter((m) => !blockedSet.has(m.userId));
  }, [messages, blockedSet]);

  useEffect(() => {
    if (scrollContainerRef.current) {
      scrollContainerRef.current.scrollTo({
        top: scrollContainerRef.current.scrollHeight,
        behavior: "smooth",
      });
    }
  }, [visibleMessages, scrollContainerRef]);

  const handleBlock = async (userId: string, userNickname: string) => {
    try {
      await blockUser(userId);
      // 전역에도 즉시 반영 → 현재/과거 메시지 즉시 숨김
      blockLocal(userId);
      setModalInfo({
        isOpen: true,
        title: "차단 완료",
        description: `${userNickname}님을 차단했습니다.`,
      });
    } catch {
      setModalInfo({
        isOpen: true,
        title: "오류",
        description: "차단 요청에 실패했습니다. 다시 시도해주세요.",
      });
    }
  };

  if (!Array.isArray(messages)) {
    return (
      <div className="p-4 text-sm text-gray-500">채팅을 불러오는 중...</div>
    );
  }

  if (visibleMessages.length === 0) {
    return (
      <div className="flex items-center justify-center h-full">
        <p className="text-sm text-gray-400">아직 채팅 메시지가 없습니다.</p>
      </div>
    );
  }

  return (
    <>
      <div className="space-y-4 p-1">
        {visibleMessages.map((msg) => (
          <div
            key={msg.messageId}
            className={`flex items-start gap-3 ${
              msg.userId === myUser?.userId ? "justify-end" : "justify-start"
            }`}
          >
            <div
              className={`flex flex-col ${
                msg.userId === myUser?.userId ? "items-end" : "items-start"
              }`}
            >
              <span className="text-sm font-semibold text-gray-700 mb-1">
                {msg.userNickname}
              </span>
              <div className="flex items-end gap-2">
                {msg.userId === myUser?.userId ? (
                  <>
                    <span className="text-xs text-gray-400 self-end">
                      {new Date(msg.sentAt).toLocaleTimeString(undefined, {
                        hour: "2-digit",
                        minute: "2-digit",
                      })}
                    </span>
                    <div className="relative group px-4 py-2 rounded-xl max-w-xs break-words bg-purple-600 text-white rounded-br-none">
                      <span>{msg.content}</span>
                    </div>
                  </>
                ) : (
                  <>
                    <div className="relative group px-4 py-2 rounded-xl max-w-xs break-words bg-gray-200 text-gray-800 rounded-bl-none">
                      <span>{msg.content}</span>
                      <Popover className="absolute top-0 right-0">
                        <Popover.Button className="p-1 rounded-full text-gray-500 opacity-0 group-hover:opacity-100 transition-opacity focus:outline-none focus:opacity-100 hover:bg-black/10">
                          <MoreVertical size={16} />
                        </Popover.Button>
                        <Popover.Panel className="absolute z-10 top-0 left-full ml-1 w-32 bg-white border rounded-lg shadow-lg">
                          <div className="p-1">
                            <button
                              onClick={() =>
                                handleBlock(msg.userId, msg.userNickname)
                              }
                              className="flex items-center gap-2 w-full px-3 py-1.5 text-sm text-left text-gray-700 hover:bg-gray-100 rounded-md"
                            >
                              <UserX size={14} />
                              <span>차단하기</span>
                            </button>
                          </div>
                        </Popover.Panel>
                      </Popover>
                    </div>
                    <span className="text-xs text-gray-400 self-end">
                      {new Date(msg.sentAt).toLocaleTimeString(undefined, {
                        hour: "2-digit",
                        minute: "2-digit",
                      })}
                    </span>
                  </>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>

      <ConfirmModal
        isOpen={modalInfo.isOpen}
        title={modalInfo.title}
        description={modalInfo.description}
        confirmText="확인"
        onConfirm={() => setModalInfo({ ...modalInfo, isOpen: false })}
        onCancel={() => setModalInfo({ ...modalInfo, isOpen: false })}
      />
    </>
  );
};

export default ArtistChatTab;
