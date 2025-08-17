import { User, Pencil } from "lucide-react";
import { useEffect, useRef, useState } from "react";

type LiveHeaderProps = {
  isHost: boolean;
  title: string;
  hostId: string;
  hostNickname: string;
  participantCount: number;
  onExit: () => void;
  onDelete?: () => void;
  onSaveTitle: (nextTitle: string) => Promise<void> | void;
};

const LiveHeader = ({
  isHost,
  title,
  hostNickname,
  participantCount,
  onExit,
  onDelete,
  onSaveTitle,
}: LiveHeaderProps) => {
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState(title);
  const [saving, setSaving] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => setDraft(title), [title]);
  useEffect(() => {
    if (editing) inputRef.current?.focus();
  }, [editing]);

  const doSave = async () => {
    const next = draft.trim();
    if (!next || next === title) {
      setEditing(false);
      setDraft(title);
      return;
    }
    try {
      setSaving(true);
      await onSaveTitle(next);
      setEditing(false);
    } finally {
      setSaving(false);
    }
  };

  const onPencilClick = () => {
    if (!isHost || saving) return;
    if (!editing) {
      setEditing(true);
      setDraft(title);
    } else {
      void doSave(); // 편집 중 다시 클릭 → 저장
    }
  };

  const onKeyDown: React.KeyboardEventHandler<HTMLInputElement> = (e) => {
    if (e.key === "Enter") {
      e.preventDefault();
      void doSave();
    } else if (e.key === "Escape") {
      e.preventDefault();
      setEditing(false);
      setDraft(title);
    }
  };

  return (
    <div className="bg-black text-white px-6 py-3 flex justify-between items-center border-b border-gray-800">
      {/* 왼쪽: 제목(1행) + 부가정보(2행) */}
      <div className="flex flex-col">
        <div className="flex items-center gap-2">
          {editing ? (
            <input
              ref={inputRef}
              value={draft}
              onChange={(e) => setDraft(e.target.value)}
              onKeyDown={onKeyDown}
              maxLength={50}
              className="text-xl font-bold tracking-tight bg-transparent text-white border border-purple-500/60 rounded-lg px-3 py-1 outline-none focus:border-purple-400"
              placeholder="방 제목을 입력하세요"
              disabled={saving}
            />
          ) : (
            <h1 className="text-xl font-bold tracking-tight">
              {title || "제목 없음"}
            </h1>
          )}

          {/* 연필(호스트만) */}
          <button
            type="button"
            onClick={onPencilClick}
            title={editing ? "제목 저장" : "제목 수정"}
            className={`ml-1 p-1 rounded-lg border ${
              isHost
                ? "border-purple-500/60 hover:border-purple-400 hover:bg-purple-500/10"
                : "border-gray-700 opacity-50 cursor-not-allowed"
            }`}
            disabled={!isHost || saving}
          >
            <Pencil className={`w-4 h-4 ${saving ? "animate-pulse" : ""}`} />
          </button>
        </div>

        {/* 제목 아래 보조 정보 */}
        <div className="text-sm text-gray-400 mt-1.5 flex items-center gap-x-4">
          <span>호스트: {hostNickname || "알 수 없음"}</span>
          <div className="flex items-center gap-x-1.5">
            <User size={15} className="text-gray-500" />
            <span>{participantCount}</span>
          </div>
        </div>
      </div>

      {/* 오른쪽 버튼들 */}
      <div className="flex items-center gap-x-3">
        {isHost ? (
          onDelete && (
            <button
              type="button"
              onClick={onDelete}
              className="bg-red-600 hover:bg-red-700 text-white font-semibold px-5 py-2.5 rounded-lg transition-colors focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2 focus:ring-offset-black"
            >
              방 삭제
            </button>
          )
        ) : (
          <button
            type="button"
            onClick={onExit}
            className="bg-purple-600 hover:bg-purple-700 text-white font-semibold px-5 py-2.5 rounded-lg transition-colors focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2 focus:ring-offset-black"
          >
            나가기
          </button>
        )}
      </div>
    </div>
  );
};

export default LiveHeader;
