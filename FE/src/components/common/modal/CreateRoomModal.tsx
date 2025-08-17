import {useState, useEffect} from "react";
import {CreateRoom, enterRoom} from "../../../api/roomService";
import {useNavigate} from "react-router-dom";
import {X} from "lucide-react";
import {fetchYouTubeMeta} from "../../../utils/youtubeMeta";



// 다양한 YouTube URL에서 videoId를 추출하는 함수
const extractVideoId = (url: string): string | null => {
  const match = url.match(
    /(?:youtu\.be\/|youtube\.com\/(?:watch\?v=|embed\/|v\/|shorts\/))([\w-]{11})/
  );
  return match ? match[1] : null;
};

type CreateRoomModalProps = {
  isOpen: boolean;
  onClose: () => void;
  artistId: number;
  hostId: string;
  hostNickname: string;
};

const CreateRoomModal = ({
  isOpen,
  onClose,
  artistId,
  hostId,
  hostNickname
}: CreateRoomModalProps) => {
  const [title, setTitle] = useState("");
  const [locked, setLocked] = useState(false);
  const [entryQuestion, setEntryQuestion] = useState("");
  const [entryAnswer, setEntryAnswer] = useState("");
  const [videoUrl, setVideoUrl] = useState("");
  const [videoId, setVideoId] = useState<string | null>(null);
  const [thumbnailPreview, setThumbnailPreview] = useState<string | null>(null);
  const [errors, setErrors] = useState<string>("");
  const [videoMeta, setVideoMeta] = useState<{title?: string; author?: string} | null>(null);

  const navigate = useNavigate();

  useEffect(() => {
    const id = extractVideoId(videoUrl);
    setVideoId(id);
    if (id) {
      setThumbnailPreview(`https://img.youtube.com/vi/${id}/hqdefault.jpg`);
      fetchYouTubeMeta(id).then((m) => m && setVideoMeta({title: m.title, author: m.author}));
    } else {
      setThumbnailPreview(null);
      setVideoMeta(null);
    }
  }, [videoUrl]);

  const handleSubmit = async () => {
    if (
      !title ||
      !videoUrl ||
      !videoId ||
      (locked && (!entryQuestion || !entryAnswer))
    ) {
      setErrors("모든 필수 항목을 입력해주세요.");
      return;
    }

    const formData = new FormData();
    formData.append("artistId", artistId.toString());
    formData.append("title", title);
    formData.append("hostId", hostId);
    formData.append("locked", locked.toString());
    formData.append("videoId", videoId);
    formData.append("hostNickname", hostNickname)

    if (thumbnailPreview) {
      const blob = await fetch(thumbnailPreview).then((res) => res.blob());
      const file = new File([blob], "thumbnail.jpg", {type: blob.type});
      formData.append("thumbnailImg", file);
    }

    if (locked) {
      formData.append("entryQuestion", entryQuestion);
      formData.append("entryAnswer", entryAnswer);
    }

    try {
      const createdRoom = await CreateRoom(formData);
      try {
        await enterRoom(String(createdRoom.roomId), locked ? entryAnswer : "");
      } catch (err: any) {
        const status = err?.response?.status;
        if (status === 409) {
          // 이미 집계됨/중복 입장 같은 정책이면 무시
        } else if (status === 401) {
          alert(
            "로그인이 만료되었거나 유효하지 않습니다. 다시 로그인해주세요."
          );
        }
      }

      onClose();
      navigate(`/live/${createdRoom.roomId}`, {
        state: {
          artistId,
          isHost: true,
          entryAnswer: locked ? entryAnswer : undefined, // 잠금 방 대비
        },
      });
    } catch {
      alert("방 생성에 실패했습니다.");
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 overflow-auto">
      {/* 모달 프레임 */}
      <div className="bg-white rounded-xl shadow-lg w-full max-w-lg max-h-[90vh] flex flex-col overflow-hidden">
        {/* 모달 헤더 */}
        <div className="flex justify-between items-center p-5 border-b border-gray-200">
          <h2 className="text-xl font-semibold">새 방 만들기</h2>
          <button
            className="text-gray-400 hover:text-gray-800"
            onClick={onClose}
          >
            <X size={24} />
          </button>
        </div>

        {/* 모달 본문 */}
        <div className="flex-1 overflow-y-auto p-6 space-y-5">
          {errors && (
            <div className="text-red-500 text-sm text-center">{errors}</div>
          )}

          <div>
            <label className="block text-sm font-medium mb-1">
              방 제목<span className="text-red-500">*</span>
            </label>
            <input
              className="w-full border rounded px-3 py-2"
              placeholder="방 제목을 입력해주세요"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
            />
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">
              YouTube URL<span className="text-red-500">*</span>
            </label>
            <input
              className="w-full border rounded px-3 py-2"
              placeholder="유튜브 링크를 입력해주세요"
              value={videoUrl}
              onChange={(e) => setVideoUrl(e.target.value)}
            />
          </div>

          {thumbnailPreview && (
            <div className="aspect-[16/9] bg-black rounded-lg overflow-hidden shadow-lg">
              <img
                src={thumbnailPreview}
                alt="썸네일 미리보기"
                className="w-full h-full object-cover"
              />
            </div>
          )}

          {videoMeta && (videoMeta.title || videoMeta.author) && (
            <div className="mt-2 text-sm text-gray-800">
              <div className="font-semibold truncate">{videoMeta.title || "제목 로딩 중..."}</div>
              <div className="text-gray-600 truncate">{videoMeta.author || "채널 로딩 중..."}</div>
            </div>
          )}

          <div>
            <label className="block text-sm font-medium mb-1">
              비밀번호 설정 여부
            </label>
            <div className="flex gap-6 mt-1">
              <label className="flex items-center gap-1">
                <input
                  type="radio"
                  name="locked"
                  checked={locked}
                  onChange={() => setLocked(true)}
                />
                예
              </label>
              <label className="flex items-center gap-1">
                <input
                  type="radio"
                  name="locked"
                  checked={!locked}
                  onChange={() => setLocked(false)}
                />
                아니요
              </label>
            </div>
          </div>

          {locked && (
            <>
              <div>
                <label className="block text-sm font-medium mb-1">
                  입장 질문<span className="text-red-500">*</span>
                </label>
                <input
                  className="w-full border rounded px-3 py-2"
                  placeholder="1+1=?"
                  value={entryQuestion}
                  onChange={(e) => setEntryQuestion(e.target.value)}
                />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">
                  정답<span className="text-red-500">*</span>
                </label>
                <input
                  className="w-full border rounded px-3 py-2"
                  placeholder="2"
                  value={entryAnswer}
                  onChange={(e) => setEntryAnswer(e.target.value)}
                />
              </div>
            </>
          )}
        </div>

        {/* 모달 푸터 */}
        <div className="flex justify-end gap-3 p-5 border-t border-gray-200">
          <button
            className="px-5 py-2.5 border rounded-lg text-gray-700 hover:bg-gray-100 font-semibold transition"
            onClick={onClose}
          >
            취소
          </button>
          <button
            className="px-5 py-2.5 bg-purple-600 text-white rounded-lg hover:bg-purple-700 font-semibold transition"
            onClick={handleSubmit}
          >
            방 만들기
          </button>
        </div>
      </div>
    </div>
  );
};

export default CreateRoomModal;
