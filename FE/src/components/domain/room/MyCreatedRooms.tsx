import type { RoomHistory } from "../../../types/room";
import type { ReactNode } from "react";

type Props = {
  rooms: RoomHistory[];
  title?: string;
  liveRoomId?: number | null;
  onEnterLive?: (roomId: number) => void;
  /** 제목 바로 아래에 꽂을 커스텀 필터/정렬 바 */
  filters?: ReactNode;
};

const NO_IMAGE = "https://placehold.co/1280x720?text=No+Image";

const MyCreatedRooms = ({
  rooms,
  title = "내가 만든 방",
  liveRoomId,
  onEnterLive,
  filters,
}: Props) => {
  const list = (rooms ?? [])
    .slice()
    .sort(
      (a, b) =>
        new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    );

  if (!list.length) {
    return (
      <div className="bg-white rounded-xl px-8 py-6 w-full max-w-[880px] mx-auto shadow-sm">
        <h2 className="text-lg font-bold mb-2">{title}</h2>
        <p className="text-sm text-gray-500">아직 만든 방이 없습니다.</p>
      </div>
    );
  }

  const fmt = (iso: string) => {
    try {
      return new Date(iso).toLocaleString();
    } catch {
      return iso;
    }
  };

  return (
    <div className="bg-white rounded-xl px-8 py-6 w-full max-w-[880px] mx-auto shadow-sm">
      <h2 className="text-lg font-bold mb-4">{title}</h2>

      {/* 제목과 리스트 사이에 필터 바 삽입 */}
      {filters}

      <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {list.map((r) => {
          const isLive = liveRoomId != null && r.roomId === liveRoomId;
          return (
            <div
              key={r.roomId}
              className={`border border-gray-200 rounded-lg overflow-hidden hover:shadow-sm transition ${
                isLive ? "ring-2 ring-purple-500" : ""
              }`}
            >
              <div className="relative aspect-video bg-gray-50">
                <img
                  src={r.imgUrl || NO_IMAGE}
                  alt={r.title}
                  className="w-full h-full object-cover"
                  loading="lazy"
                />
                {isLive && (
                  <div className="absolute top-2 left-2 px-2 py-1 text-xs font-bold bg-red-500 text-white rounded">
                    LIVE
                  </div>
                )}
              </div>

              <div className="p-4">
                <div className="font-semibold line-clamp-1">{r.title}</div>
                <div className="mt-1 text-xs text-gray-500">
                  생성: {fmt(r.createdAt)}
                </div>

                <div className="mt-2 text-[11px] text-gray-600 flex flex-wrap gap-2">
                  <span className="px-2 py-0.5 bg-gray-100 rounded-full">
                    {r.artistNameKr ??
                      r.artistNameEn ??
                      `artistId: ${r.artistId}`}
                  </span>
                </div>

                {isLive && (
                  <button
                    onClick={() => onEnterLive?.(r.roomId)}
                    className="mt-3 w-full px-3 py-2 rounded-md text-sm font-semibold bg-purple-600 text-white hover:bg-purple-700"
                  >
                    입장하기
                  </button>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default MyCreatedRooms;
