import { api, getAccessToken, buildApiUrl } from "./axiosInstance";
import type { room, TrendingRoomsResponse } from "../types/room";

// 타입
export type PlaylistUpdateReq = {
  playlist: string[];
  nextIndex?: number; // optional (default=0 on server)
};

// 방 생성
export const CreateRoom = async (formData: FormData) => {
  const response = await api.post("/rooms", formData);
  return response.data;
};

// 방 정보 조회(공개)
export const fetchRoomById = async (roomId: string) => {
  const response = await api.get(`/rooms/${roomId}`, { skipAuth: true });
  return response.data;
};

// 특정 아티스트의 방 목록
export const getRoomsByArtist = async (artistId: number): Promise<room[]> => {
  const response = await api.get(`/rooms`, {
    params: { artistId },
    skipAuth: true,
  });
  return response.data.roomInfoList;
};

// 트렌딩
export const getTrendingRooms = async (
  page: number,
  size = 10
): Promise<TrendingRoomsResponse> => {
  const response = await api.get("/rooms/trending", {
    params: { page, size },
    skipAuth: true,
  });
  return response.data;
};

// 방 입장
export const enterRoom = async (roomId: string, answer: string) => {
  const access = getAccessToken();
  const res = await api.post(
    `/rooms/${roomId}/enter`,
    { entryAnswer: answer ?? "" },
    {
      headers: { "Content-Type": "application/json" },
      skipAuth: !access, // 토큰 없으면 공개 호출
    }
  );
  return res.data;
};

// 방 퇴장
export const exitRoom = async (roomId: number, artistId: number) => {
  const res = await api.post(`/rooms/${roomId}/exit`, null, {
    params: { artistId },
  });
  return res.data;
};

// 방 삭제
export const deleteRoom = async (roomId: number, artistId: number) => {
  const res = await api.delete(`/rooms/${roomId}`, { params: { artistId } });
  return res.data;
};

// 방 제목 변경
export const updateRoomTitle = async (
  roomId: number | string,
  title: string
): Promise<string> => {
  const { data } = await api.patch<string>(`/rooms/${roomId}/title`, null, {
    params: { title },
  });
  return data;
};

// 플레이리스트 갱신(전체 교체/삭제/순서변경)
export const updatePlaylist = async (
  roomId: number | string,
  payload: PlaylistUpdateReq
): Promise<{ message: string; lastUpdated: number }> => {
  // 1) playlist 정제
  const raw = Array.isArray(payload?.playlist) ? payload.playlist : [];
  const list = raw
    .filter((v): v is string => typeof v === "string")
    .map((v) => v.trim())
    .filter((v) => v.length > 0);

  if (list.length === 0) {
    // 서버가 빈 배열 거부. 프론트에서 차단
    throw new Error("플레이리스트는 비어있을 수 없습니다.");
  }

  // 2) nextIndex 클램프
  let nextIndex =
    typeof payload?.nextIndex === "number" && Number.isFinite(payload.nextIndex)
      ? Math.trunc(payload.nextIndex)
      : 0;
  if (nextIndex < 0) nextIndex = 0;
  if (nextIndex >= list.length) nextIndex = list.length - 1;

  // 3) 반드시 /api 프록시를 타는 절대 URL 구성
  const path = buildApiUrl(`/rooms/${roomId}/playlist`); // '/api/rooms/...'
  const absoluteUrl = path.startsWith("http")
    ? path
    : `${window.location.origin}${path}`;

  // 4) 인스턴스 baseURL 무시 (중복/누락 방지)
  const { data } = await api.post(
    absoluteUrl,
    { playlist: list, nextIndex },
    { baseURL: "" } // 인스턴스 baseURL 영향 제거
  );
  return data;
};