// src/api/translateService.ts
import axios from "axios";
import { api } from "./axiosInstance";

export type TranslateRequest = {
  message: string;
  language: string;
};

export type TranslateResponse = {
  translatedMessage: string;
};

export type TranslateErrorCode =
  | "TIMEOUT"            // 클라이언트 타임아웃(axios)
  | "UNAUTHORIZED"       // 401 (리프레시까지 실패)
  | "FORBIDDEN"          // 403
  | "RATE_LIMIT"         // 429
  | "PAYLOAD_TOO_LARGE"  // 413
  | "BAD_REQUEST"        // 400
  | "UPSTREAM_TIMEOUT"   // 504
  | "SERVER_ERROR"       // 5xx
  | "NETWORK"            // 응답 자체가 없음(CORS/네트워크)
  | "CANCELLED"          // 요청 취소
  | "NO_RESULT"          // 번역 결과 없음
  | "UNKNOWN";

export class TranslateError extends Error {
  code: TranslateErrorCode;
  status?: number;
  constructor(code: TranslateErrorCode, status?: number, message?: string) {
    super(message || code);
    this.name = "TranslateError";
    this.code = code;
    this.status = status;
  }
}

/**
 * 번역 요청 (기본 타깃 ko)
 * - Promise<string> 유지 (호출부 계약 유지)
 * - 타임아웃은 axios 인터셉터에서 /translate만 50s로 오버라이드됨
 */
export async function translateMessage(
  message: string,
  language: string = "ko"
): Promise<string> {
  try {
    const { data } = await api.post<TranslateResponse>(
      "/translate",
      { message, language } satisfies TranslateRequest
    );

    if (!data?.translatedMessage) {
      throw new TranslateError("NO_RESULT");
    }
    return data.translatedMessage;
  } catch (e) {
    // 1) 요청 취소 먼저 처리
    if (axios.isCancel(e)) {
      throw new TranslateError("CANCELLED");
    }

    // 2) Axios 에러 세부 분기
    if (axios.isAxiosError(e)) {
      const code = e.code;                 // string | undefined
      const status = e.response?.status;   // number | undefined

      if (code === "ERR_CANCELED") {
        throw new TranslateError("CANCELLED");
      }
      if (code === "ECONNABORTED") {
        throw new TranslateError("TIMEOUT");
      }

      if (typeof status === "number") {
        if (status === 401) throw new TranslateError("UNAUTHORIZED", status);
        if (status === 403) throw new TranslateError("FORBIDDEN", status);
        if (status === 413) throw new TranslateError("PAYLOAD_TOO_LARGE", status);
        if (status === 429) throw new TranslateError("RATE_LIMIT", status);
        if (status === 400) throw new TranslateError("BAD_REQUEST", status);
        if (status === 504) throw new TranslateError("UPSTREAM_TIMEOUT", status);
        if (status >= 500) throw new TranslateError("SERVER_ERROR", status);
      }

      // 요청은 갔지만 응답 객체가 전혀 없음 (네트워크/CORS 차단 등)
      if (!e.response) {
        throw new TranslateError("NETWORK");
      }
    }

    // 3) 최후의 수단
    throw new TranslateError("UNKNOWN");
  }
}
