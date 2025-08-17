import { useEffect, type RefObject } from "react";
import type { artistChatMessage } from "../../types/artistChat";
import { useUserStore } from "../../store/useUserStore";
import { Popover } from "@headlessui/react";
import { MoreVertical, UserX } from "lucide-react";
import { blockUser } from "../../api/userService";

type ChatTabProps = {
  messages: artistChatMessage[];
  scrollContainerRef: RefObject<HTMLDivElement | null>;
};

const ArtistChatTab = ({ messages, scrollContainerRef }: ChatTabProps) => {
  const { myUser } = useUserStore();

  useEffect(() => {
    if (scrollContainerRef.current) {
      scrollContainerRef.current.scrollTo({
        top: scrollContainerRef.current.scrollHeight,
        behavior: "smooth",
      });
    }
  }, [messages, scrollContainerRef]);

  // 차단 버튼 핸들러
  const handleBlock = async (userId: string, userNickname: string) => {
    if (window.confirm(`${userNickname}님을 정말로 차단하시겠습니까?`)) {
      try {
        await blockUser(userId);
        alert(`${userNickname}님을 차단했습니다.`);
      } catch {
        alert("차단 요청에 실패했습니다. 다시 시도해주세요.");
      }
    }
  };

  if (!Array.isArray(messages)) {
    return (
      <div className="p-4 text-sm text-gray-500">채팅을 불러오는 중...</div>
    );
  }

  if (messages.length === 0) {
    return (
      <div className="flex items-center justify-center h-full">
        <p className="text-sm text-gray-400">아직 채팅 메시지가 없습니다.</p>
      </div>
    );
  }

  return (
    <div className="space-y-4 p-1">
      {messages.map((msg) => (
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
                // 내가 보낸 메시지: 시간이 왼쪽
                <>
                  <span className="text-xs text-gray-400 self-end">
                    {new Date(msg.sentAt).toLocaleTimeString(undefined, {
                      hour: "2-digit",
                      minute: "2-digit",
                    })}
                  </span>
                  <div
                    className="relative group px-4 py-2 rounded-xl max-w-xs break-words
        bg-purple-600 text-white rounded-br-none"
                  >
                    <span>{msg.content}</span>
                  </div>
                </>
              ) : (
                // 남이 보낸 메시지: 시간은 오른쪽
                <>
                  <div
                    className="relative group px-4 py-2 rounded-xl max-w-xs break-words
        bg-gray-200 text-gray-800 rounded-bl-none"
                  >
                    <span>{msg.content}</span>

                    {/* Popover (더보기 메뉴) */}
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
  );
};

export default ArtistChatTab;
