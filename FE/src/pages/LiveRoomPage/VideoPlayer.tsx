import React, { useEffect, useRef, useState } from "react";
import YouTube from "react-youtube";
import { Client } from "@stomp/stompjs";
import type { User } from "../../types";
import type { LiveRoomSyncDTO } from "../../types/room";

type VideoPlayerProps = {
  videoId: string;
  isHost: boolean;
  stompClient: Client;
  user: User;
  roomId: number;
  playlist: string[];
  currentVideoIndex: number;
  isPlaylistUpdating: boolean;
  onVideoEnd: () => void;
  roomTitle: string;
  hostNickname?: string | null;
};

// 드리프트 보정 파라미터
const DRIFT_TOLERANCE_HARD = 2.5;
const DRIFT_TOLERANCE_SOFT = 0.4;
const SOFT_CORRECT_MS = 1500;
const HEARTBEAT_MS = 5000;

const VideoPlayer: React.FC<VideoPlayerProps> = ({
  videoId,
  isHost,
  stompClient,
  user,
  roomId,
  playlist,
  currentVideoIndex,
  isPlaylistUpdating,
  onVideoEnd,
  roomTitle,
  hostNickname,
}) => {
  const playerRef = useRef<YT.Player | null>(null);

  // 이 탭(session)에서 이미 '사운드 켜기'를 눌렀는지 여부 (탭 닫힐 때까지 유지)
  const initialUnlocked = (() => {
    try {
      return sessionStorage.getItem("audioUnlocked") === "1";
    } catch {
      return false;
    }
  })();
  const audioUnlockedRef = useRef<boolean>(initialUnlocked);

  // 시청 관련 상태
  const [canWatch, setCanWatch] = useState(false); // 참가자 재생 허용(재생 중) 여부
  const justSynced = useRef(false); // sync 직후 onStateChange 가드

  // 언락되어 있으면 시작부터 비뮤트
  const [muted, setMuted] = useState<boolean>(!audioUnlockedRef.current);
  const [showUnmuteHint, setShowUnmuteHint] = useState(false); // "사운드 켜기" 안내 버튼

  // 소프트 보정 타이머
  const rateTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const clearRateTimer = () => {
    if (rateTimerRef.current) {
      clearTimeout(rateTimerRef.current);
      rateTimerRef.current = null;
    }
  };
  const restoreNormalRate = () => {
    const p = playerRef.current;
    if (!p) return;
    try {
      if (typeof p.getPlaybackRate === "function" && p.getPlaybackRate() !== 1) {
        p.setPlaybackRate(1);
      }
    } catch {}
  };

  // ENDED 중복 호출 방지
  const endFiredRef = useRef(false);

  const onPlayerReady = async (event: YT.PlayerEvent) => {
    const p = event.target;
    playerRef.current = p;

    try {
      if (isHost) {
        // 방장: 소리 ON 자동재생 시도, 거부 시 아래 catch로 폴백
        p.unMute?.();
        p.setVolume?.(100);
        setMuted(false);
        await p.playVideo();
        setCanWatch(true);
      } else {
        // 참가자
        if (audioUnlockedRef.current) {
          // 이미 언락된 참가자는 다시는 mute로 돌리지 않음
          p.unMute?.();
          p.setVolume?.(100);
          setMuted(false);
          await p.playVideo();
          setCanWatch(true);
        } else {
          // 아직 언락 전이면 정책상 mute 시작
          p.mute?.();
          p.setVolume?.(100);
          setMuted(true);
          p.pauseVideo();
        }
      }
    } catch {
      // 자동재생 거부 → mute 재생으로 폴백
      try {
        p.mute?.();
        setMuted(!audioUnlockedRef.current);
        await p.playVideo();
        setCanWatch(true);
      } catch {
        p.pauseVideo();
        setCanWatch(false);
      }
    }
  };

  const onPlayerStateChange = (event: YT.OnStateChangeEvent) => {
    if (event.data === YT.PlayerState.ENDED) {
      if (!isHost) return;
      if (endFiredRef.current) return;
      endFiredRef.current = true;
      onVideoEnd();
      return;
    }

    if (event.data === YT.PlayerState.PLAYING) {
      endFiredRef.current = false; // 다음 종료 감지 준비
    }

    const player = playerRef.current;
    if (!stompClient.connected || !player) return;

    // 참가자: 임의 조작 차단(호스트만 컨트롤)
    if (!isHost) {
      if (event.data === YT.PlayerState.PAUSED && canWatch) {
        try {
          player.playVideo();
        } catch {}
        return;
      }
      if (event.data === YT.PlayerState.PLAYING) {
        if (justSynced.current) {
          justSynced.current = false;
          return;
        }
        if (!canWatch) player.pauseVideo();
      }
      return;
    }

    // 방장: 상태 브로드캐스트
    const payload: LiveRoomSyncDTO = {
      eventType: "SYNC_STATE",
      roomId,
      hostId: user.userId,
      title: roomTitle,
      hostNickname: hostNickname ?? user.nickname ?? "",
      playlist,
      currentVideoIndex,
      currentTime: player.getCurrentTime(),
      playing: event.data === YT.PlayerState.PLAYING,
      lastUpdated: Date.now(),
    };

    stompClient.publish({
      destination: "/app/room/update",
      body: JSON.stringify(payload),
    });
  };

  // 참가자: SYNC_STATE 구독 및 드리프트 보정
  useEffect(() => {
    if (!stompClient || !stompClient.connected || isHost) return;

    const waitAndSubscribe = () => {
      if (!playerRef.current) {
        setTimeout(waitAndSubscribe, 100);
        return;
      }

      const subscription = stompClient.subscribe(
        `/topic/room/${roomId}`,
        (message) => {
          try {
            const parsed = JSON.parse(message.body) as LiveRoomSyncDTO;
            if (parsed.eventType !== "SYNC_STATE") return;

            const player = playerRef.current;
            if (!player) return;

            const now = Date.now();
            const expectedTime =
              typeof parsed.lastUpdated === "number"
                ? (parsed.currentTime ?? 0) + (now - parsed.lastUpdated) / 1000
                : parsed.currentTime ?? 0;

            // 재생 안내 버튼 (언락 전일 때만 노출)
            if (parsed.playing && muted) setShowUnmuteHint(true);

            const localTime = player.getCurrentTime();
            const delta = expectedTime - localTime;
            const absDrift = Math.abs(delta);

            if (parsed.playing) {
              if (!canWatch) {
                clearRateTimer();
                restoreNormalRate();
                player.seekTo(expectedTime, true);
                justSynced.current = true;
                player.playVideo(); // mute 상태면 자동재생 허용
                setCanWatch(true);
                return;
              }

              if (absDrift >= DRIFT_TOLERANCE_HARD) {
                clearRateTimer();
                restoreNormalRate();
                player.seekTo(expectedTime, true);
                justSynced.current = true;
              } else if (absDrift >= DRIFT_TOLERANCE_SOFT) {
                const targetRate = delta > 0 ? 1.25 : 0.75;
                try {
                  if (player.getPlaybackRate() !== targetRate) {
                    player.setPlaybackRate(targetRate);
                  }
                } catch {}
                clearRateTimer();
                rateTimerRef.current = setTimeout(() => {
                  restoreNormalRate();
                }, SOFT_CORRECT_MS);
              } else {
                clearRateTimer();
                restoreNormalRate();
              }
            } else {
              clearRateTimer();
              restoreNormalRate();
              player.pauseVideo();
              setCanWatch(false);
            }
          } catch (err) {
            console.error("참가자 메시지 파싱 실패", err);
          }
        }
      );

      return () => subscription.unsubscribe();
    };

    const cleanup = waitAndSubscribe();
    return () => {
      if (typeof cleanup === "function") cleanup();
      clearRateTimer();
      restoreNormalRate();
    };
  }, [stompClient, isHost, roomId, canWatch, muted]);

  // 방장: 하트비트
  useEffect(() => {
    if (!isHost || !stompClient.connected || isPlaylistUpdating) return;

    const interval = setInterval(() => {
      const player = playerRef.current;
      if (!player) return;

      const payload: LiveRoomSyncDTO = {
        eventType: "SYNC_STATE",
        roomId,
        hostId: user.userId,
        title: roomTitle,
        hostNickname: hostNickname ?? user.nickname ?? "",
        playlist,
        currentVideoIndex,
        currentTime: player.getCurrentTime(),
        playing: player.getPlayerState() === YT.PlayerState.PLAYING,
        lastUpdated: Date.now(),
      };

      stompClient.publish({
        destination: "/app/room/update",
        body: JSON.stringify(payload),
      });
    }, HEARTBEAT_MS);

    return () => clearInterval(interval);
  }, [
    isHost,
    stompClient,
    isPlaylistUpdating,
    user,
    roomId,
    playlist,
    currentVideoIndex,
  ]);

  // 참가자: "사운드 켜기"
  const handleUnmute = () => {
    const p = playerRef.current;
    if (!p) return;
    try {
      p.unMute();
      p.setVolume?.(100);
      setMuted(false);
      setShowUnmuteHint(false);
      // 이 탭에서 이후로는 계속 소리 ON 유지
      audioUnlockedRef.current = true;
      sessionStorage.setItem("audioUnlocked", "1");
    } catch (e) {
      console.warn("unMute 실패", e);
    }
  };

  // 이미 언락된 참가자는 처음부터 비뮤트로 시작
  const initialMute = isHost ? 0 : (audioUnlockedRef.current ? 0 : 1);

  return (
    <div className="relative w-full h-full bg-black">
      {videoId ? (
        <>
          <YouTube
            videoId={videoId}
            onReady={onPlayerReady}
            onStateChange={onPlayerStateChange}
            className="w-full h-full"
            opts={{
              width: "100%",
              height: "100%",
              playerVars: {
                autoplay: isHost ? 1 : 0, // 방장은 즉시 재생 시도
                mute: initialMute,        // 참가자: 언락 시 비뮤트로 시작
                controls: isHost ? 1 : 0, // 참가자 조작 불가
                disablekb: 1,             // 키보드 조작 차단
                rel: 0,
                enablejsapi: 1,
                playsinline: 1,
                modestbranding: 1,
                iv_load_policy: 3,
              },
            }}
          />

          {/* 참가자: 호스트 재생 중 + 음소거 상태면 "사운드 켜기" 버튼 */}
          {!isHost && canWatch && showUnmuteHint && muted && (
            <div className="absolute bottom-4 left-1/2 -translate-x-1/2 z-20">
              <button
                onClick={handleUnmute}
                className="px-4 py-2 bg-fuchsia-600 hover:bg-fuchsia-700 text-white text-sm rounded-lg shadow-lg"
              >
                사운드 켜기
              </button>
            </div>
          )}

          {/* 참가자: 호스트 play 전 대기 오버레이 */}
          {!isHost && !canWatch && (
            <div className="absolute inset-0 z-10 pointer-events-auto">
              <div className="absolute inset-0 bg-black/40" />
              <div className="relative z-20 w-full h-full flex items-center justify-center text-white text-lg">
                방장이 영상을 재생할 때까지 대기 중입니다...
              </div>
            </div>
          )}
        </>
      ) : (
        <div className="text-center text-white mt-10">영상 ID가 없습니다.</div>
      )}
    </div>
  );
};

export default VideoPlayer;
