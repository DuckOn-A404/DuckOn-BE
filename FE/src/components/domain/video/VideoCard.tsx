import { useNavigate } from "react-router-dom";
import { Users } from "lucide-react";
import { formatCompactNumber } from "../../../utils/formatters";
import { type room } from "../../../types/room";

const PLACEHOLDER_URL =
  "https://placehold.co/1280x720?text=No+Image&font=roboto";

/* 
  name : VideoCard
  summary : 방송 목록을 보여줄 때 사용하는 비디오 카드 컴포넌트, Live 여부에 따라 뱃지 등의 디자인이 변경됨
  props
    - isLive : 라이브 방송 여부 (방송 중/방송 예약)
    - viewerCount : 시청자 수 (1,000 단위 이상은 K로 포매팅 됨)
    - artistName : 방송 아티스트 이름 (아티스트 상세 페이지에서는 방장 닉네임으로 가능하도록 수정 필요)
    - title : 방 제목
*/
const VideoCard = (room: room) => {
  const navigate = useNavigate();
  const thumbnailUrl = room.imgUrl || PLACEHOLDER_URL;

  const handleCardClick = () => {
    if (room.roomId) {
      navigate(`/live/${room.roomId}`);
    }
  };

  return (
    <div
      className="w-full max-w-sm cursor-pointer group bg-white rounded-2xl shadow-md overflow-hidden transition-all duration-300 ease-in-out hover:shadow-xl hover:scale-105"
      onClick={handleCardClick}
    >
      {/* 2. 썸네일 영역 */}
      <div className="w-full relative aspect-[16/9] overflow-hidden">
        <img
          className="w-full h-full object-cover"
          src={thumbnailUrl}
          alt={`${room.title} 썸네일`}
        />
        <div className="absolute right-3 bottom-3 w-auto min-w-[4rem] h-6 inline-flex justify-center items-center gap-x-1 px-2 rounded bg-black/70 text-xs text-white">
          <Users className="h-3 w-3" />
          <span>{formatCompactNumber(room.participantCount)}</span>
        </div>
      </div>

      {/* 3. 정보 영역 (썸네일 아래) */}
      <div className="w-full p-3 flex items-start gap-3">
        {/* 방장 프로필 이미지 */}
        <img
          src={room.hostProfileImgUrl || "/default_image.png"}
          alt={room.hostNickname}
          className="w-10 h-10 rounded-full object-cover flex-shrink-0"
        />
        {/* 방 제목 및 방장 닉네임 */}
        <div className="flex-1 min-w-0">
          <p
            className="w-full text-gray-800 font-bold truncate"
            title={room.title}
          >
            {room.title}
          </p>
          <p
            className="w-full text-gray-500 text-sm truncate"
            title={room.hostNickname}
          >
            {room.hostNickname}
          </p>
        </div>
      </div>
    </div>
  );
};

export default VideoCard;
