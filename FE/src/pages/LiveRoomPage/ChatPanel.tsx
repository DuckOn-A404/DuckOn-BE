import { useState, useEffect, useRef } from "react";
import {
  Send,
  MoreVertical,
  UserX,
  LockKeyhole,
  Languages,
} from "lucide-react";
import { Popover, Transition } from "@headlessui/react";
import { useUserStore } from "../../store/useUserStore";
import type { ChatMessage } from "../../types/chat";
import { translateMessage } from "../../api/translateService";
import { blockUser } from "../../api/userService";

// --- 부모로부터 받아야 할 Props 타입 정의 ---
type ChatPanelProps = {
  messages: ChatMessage[];
  sendMessage: (content: string) => void;
  onBlockUser: (userId: string) => void;
};

// --- 번역 상태를 관리하기 위한 타입 ---
export type TranslateRequest = {
  message: string;
  language: string;
};

// 번역 타입
type TranslationState = {
  loading: boolean;
  text?: string;
  error?: string;
  showingTranslated?: boolean;
};

// --- 차단 확인 모달 컴포넌트 ---
const ConfirmModal = ({
  isOpen,
  onConfirm,
  onCancel,
  nickname,
}: {
  isOpen: boolean;
  onConfirm: () => void;
  onCancel: () => void;
  nickname: string;
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50">
      <div className="bg-gray-700 rounded-lg p-6 shadow-xl w-full max-w-sm">
        <h3 className="text-lg font-bold text-white">사용자 차단</h3>
        <p className="text-sm text-gray-300 mt-2">
          정말로{" "}
          <span className="font-semibold text-purple-400">{nickname}</span>님을
          차단하시겠습니까? <br />
          차단하면 이 사용자의 메시지가 더 이상 보이지 않습니다.
        </p>
        <div className="mt-6 flex justify-end gap-3">
          <button
            onClick={onCancel}
            className="px-4 py-2 text-sm font-medium text-gray-300 bg-gray-600 hover:bg-gray-500 rounded-md transition-colors"
          >
            취소
          </button>
          <button
            onClick={onConfirm}
            className="px-4 py-2 text-sm font-medium text-white bg-red-600 hover:bg-red-700 rounded-md transition-colors"
          >
            차단
          </button>
        </div>
      </div>
    </div>
  );
};

const ChatPanel = ({ messages, sendMessage, onBlockUser }: ChatPanelProps) => {
  const { myUser } = useUserStore();
  const [newMessage, setNewMessage] = useState("");
  const messagesEndRef = useRef<HTMLDivElement | null>(null);
  const inputRef = useRef<HTMLInputElement | null>(null);
  const sentByPointerRef = useRef(false);

  // 번역된 메시지들을 관리하는 상태
  // { 메시지키: { loading: false, text: "번역된 내용" } }
  const [translations, setTranslations] = useState<
    Record<string, TranslationState>
  >({});

  // 차단 확인 모달 상태
  const [blockConfirm, setBlockConfirm] = useState<{
    isOpen: boolean;
    user: { id: string; nickname: string } | null;
  }>({ isOpen: false, user: null });

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  const handleSendMessage = () => {
    const v = newMessage.trim();
    if (!v) return;

    sendMessage(v);
    setNewMessage("");

    // 전송 직후 다시 포커스 (렌더 한 프레임 뒤에)
    requestAnimationFrame(() => {
      inputRef.current?.focus({ preventScroll: true });
    });
  };

  // 번역 API 호출 함수 (수정됨)
  const handleTranslate = async (messageKey: string, text: string) => {
    const current = translations[messageKey];

    // 번역문이 이미 있다면 API 재호출 없이 토글만
    if (current?.text && !current.loading) {
      setTranslations((prev) => ({
        ...prev,
        [messageKey]: {
          ...current,
          showingTranslated: !current.showingTranslated,
        },
      }));
      return;
    }

    // 이미 로딩 중이면 무시
    if (current?.loading) return;

    // 최초 번역 호출
    setTranslations((prev) => ({
      ...prev,
      [messageKey]: { loading: true, showingTranslated: false },
    }));

    try {
      const targetLang = (myUser?.language || "ko").toLowerCase();
      console.log("[translate] target:", targetLang, "msg:", text);
      const translated = await translateMessage(text, targetLang);

      setTranslations((prev) => ({
        ...prev,
        [messageKey]: {
          loading: false,
          text: translated,
          showingTranslated: true, // 첫 클릭 후 번역문 보여주기
        },
      }));
    } catch (err) {
      console.error("Translation error:", err);
      setTranslations((prev) => ({
        ...prev,
        [messageKey]: {
          loading: false,
          error: "번역 실패",
          showingTranslated: false,
        },
      }));
    }
  };

  // 차단 확인 모달 열기
  const openBlockConfirm = (user: { id: string; nickname: string }) => {
    setBlockConfirm({ isOpen: true, user });
  };

  // 차단 확정
  const confirmBlock = async () => {
    if (!blockConfirm.user) return;
    const id = blockConfirm.user.id;

    onBlockUser(id);
    try {
      const res = await blockUser(id);
      console.log(res.message);
    } catch (err) {
      console.error("차단 실패:", err);
    } finally {
      setBlockConfirm({ isOpen: false, user: null });
    }
  };

  return (
    <>
      <ConfirmModal
        isOpen={blockConfirm.isOpen}
        onConfirm={confirmBlock}
        onCancel={() => setBlockConfirm({ isOpen: false, user: null })}
        nickname={blockConfirm.user?.nickname ?? ""}
      />
      <div className="flex flex-col h-full bg-gray-800 text-white">
        {/* 메시지 목록 영역 */}
        {/* <div className="flex-1 space-y-4 overflow-y-auto p-4"> */}
        <div className="flex-1 space-y-4 overflow-y-auto overscroll-contain p-4 min-h-0">
          {messages.map((msg, index) => {
            if (msg.chatType === "ENTER") {
              return (
                <div
                  key={`system-${index}`}
                  className="text-center text-xs text-gray-500 py-1"
                >
                  {msg.content}
                </div>
              );
            }

            const uniqueKey = `${msg.senderId}-${msg.sentAt || index}`;
            const isMyMessage = msg.senderId === myUser?.userId;
            const t = translations[uniqueKey];

            const contentToShow =
              t?.showingTranslated && t?.text ? t.text : msg.content;

            // const contentToShow = msg.content;

            const translateBtnLabel = t?.loading
              ? "번역 중..."
              : t?.text
              ? t.showingTranslated
                ? "되돌리기"
                : "번역하기"
              : "번역하기";

            return (
              <div
                key={uniqueKey}
                className={`flex flex-col ${
                  isMyMessage ? "items-end" : "items-start"
                }`}
              >
                <span className="text-xs text-gray-500 mb-1">
                  {msg.senderNickName}
                </span>
                <div
                  className={`group relative flex items-end gap-2 max-w-[85%] ${
                    isMyMessage ? "flex-row-reverse" : "flex-row"
                  }`}
                >
                  {/* 메시지 말풍선 */}
                  <div
                    className={`relative group px-4 py-2 rounded-lg text-sm ${
                      isMyMessage ? "bg-purple-600" : "bg-gray-700"
                    } break-all`}
                  >
                    {/* 말풍선 내용 */}
                    {t?.loading ? (
                      <span className="text-gray-300 italic">번역 중...</span>
                    ) : t?.error ? (
                      <span className="text-red-400">{t.error}</span>
                    ) : (
                      <span className={!isMyMessage ? "pr-5" : ""}>
                        {contentToShow}
                      </span>
                    )}

                    {/* <span className={!isMyMessage ? "pr-5" : ""}>
                      {contentToShow}
                    </span> */}

                    {/* 다른 사람 메시지에만 점 3개 아이콘 표시 (위치 변경됨) */}
                    {!isMyMessage && (
                      <Popover className="absolute top-1 right-1">
                        <Popover.Button className="opacity-0 group-hover:opacity-100 transition-opacity p-0.5 rounded-full hover:bg-black/20 focus:outline-none">
                          <MoreVertical size={14} className="text-white" />
                        </Popover.Button>
                        <Transition
                          enter="transition duration-100 ease-out"
                          enterFrom="transform scale-95 opacity-0"
                          enterTo="transform scale-100 opacity-100"
                          leave="transition duration-75 ease-out"
                          leaveFrom="transform scale-100 opacity-100"
                          leaveTo="transform scale-95 opacity-0"
                        >
                          <Popover.Panel className="absolute z-10 top-0 left-full ml-2 w-40 bg-gray-600 border border-gray-500 rounded-lg shadow-lg">
                            <div className="flex flex-col p-1">
                              <button
                                onClick={() =>
                                  handleTranslate(uniqueKey, msg.content)
                                }
                                className="flex items-center gap-2 w-full px-3 py-2 text-sm text-left text-gray-200 hover:bg-purple-600 rounded-md"
                              >
                                <Languages size={14} />
                                <span className="whitespace-nowrap">
                                  {translateBtnLabel}
                                </span>
                              </button>
                              <button
                                onClick={() =>
                                  openBlockConfirm({
                                    id: msg.senderId,
                                    nickname: msg.senderNickName,
                                  })
                                }
                                className="flex items-center gap-2 w-full px-3 py-2 text-sm text-left text-gray-200 hover:bg-purple-600 rounded-md"
                              >
                                <UserX size={14} />
                                <span className="whitespace-nowrap">
                                  차단하기
                                </span>
                              </button>
                            </div>
                          </Popover.Panel>
                        </Transition>
                      </Popover>
                    )}
                  </div>

                  {/* 타임스탬프 */}
                  <span className="text-xs text-gray-500 whitespace-nowrap">
                    {new Date(msg.sentAt).toLocaleTimeString("ko-KR", {
                      hour: "2-digit",
                      minute: "2-digit",
                    })}
                  </span>
                </div>
              </div>
            );
          })}
          <div ref={messagesEndRef} />
        </div>

        {/* 메시지 입력 영역 */}
        <div className="p-3 border-t border-gray-700 bg-gray-800/80">
          {myUser ? (
            <div className="relative flex items-center">
              <input
                ref={inputRef}
                type="text"
                value={newMessage}
                onChange={(e) => setNewMessage(e.target.value)}
                // onKeyDown={(e) => e.key === "Enter" && handleSendMessage()}
                onKeyDown={(e) => {
                  // 한글 조합 중 엔터 예외
                  // @ts-ignore
                  if (e.key === "Enter" && !e.nativeEvent?.isComposing) {
                    e.preventDefault();
                    handleSendMessage();
                  }
                }}
                placeholder="메시지를 입력하세요..."
                // className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2.5 text-sm outline-none focus:border-purple-500 transition-colors pr-12"
                className="w-full bg-gray-700 border border-gray-600 rounded-lg
              px-4 md:px-4 py-3 md:py-2.5
              text-base md:text-sm
              outline-none focus:border-purple-500 transition-colors pr-12"
              />
              <button
                type="button"
                tabIndex={-1}
                onPointerDown={(e) => {
                  e.preventDefault();
                }}
                onPointerUp={(e) => {
                  e.preventDefault();
                  sentByPointerRef.current = true;
                  handleSendMessage();
                }}
                onClick={(e) => {
                  if (sentByPointerRef.current) {
                    sentByPointerRef.current = false;
                    return;
                  }
                  e.preventDefault();
                  handleSendMessage();
                }}
                disabled={!newMessage.trim()}
                className="absolute right-2 top-1/2 -translate-y-1/2 p-2 bg-gray-600 rounded-full hover:bg-gray-500 transition-colors disabled:bg-gray-700 disabled:cursor-not-allowed touch-manipulation"
              >
                <Send size={18} className="text-white" />
              </button>
            </div>
          ) : (
            <div className="rounded-xl border border-gray-700 bg-gray-900/50 p-4 mx-2">
              <div className="flex items-center gap-4">
                <div className="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-lg bg-gray-800">
                  <LockKeyhole className="text-gray-500" />
                </div>
                <div>
                  <p className="text-sm font-semibold text-gray-200">
                    로그인 후 채팅 가능
                  </p>
                  <p className="text-xs text-gray-400">
                    로그인하고 대화에 참여하세요.
                  </p>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </>
  );
};

export default ChatPanel;
