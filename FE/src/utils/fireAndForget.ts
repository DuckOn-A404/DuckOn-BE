import { API_BASE, getAccessToken, buildApiUrl } from "../api/axiosInstance";

/**
 * 페이지 이탈 시에 쓰는, 응답을 기다리지 않는 가벼운 요청
 */
export function fireAndForget(
  pathOrUrl: string,
  method: "DELETE" | "POST" = "DELETE"
) {
  const token = getAccessToken() ?? "";

  // 절대/상대 경로 모두 허용
  const url =
    pathOrUrl.startsWith("http://") ||
    pathOrUrl.startsWith("https://") ||
    pathOrUrl.startsWith(API_BASE)
      ? pathOrUrl
      : buildApiUrl(pathOrUrl);

  fetch(url, {
    method,
    keepalive: true,
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  }).catch(() => {
    /* 탭 닫히는 타이밍에 실패해도 UI 영향 없음 */
  });
}
