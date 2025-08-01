package com.a404.duckonback.controller;

import com.a404.duckonback.dto.CreateRoomRequestDTO;
import com.a404.duckonback.dto.LiveRoomDTO;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.oauth.principal.CustomUserPrincipal;
import com.a404.duckonback.service.LiveRoomService;
import com.a404.duckonback.service.RedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "방 관리", description = "방 생성, 조회, 삭제 등의 기능을 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class RoomController {

    private final LiveRoomService liveRoomService;
    private final RedisService redisService;

    @Operation(summary = "방 생성",
            description = "새로운 라이브 방송 방을 생성합니다. 프로필 사진과 배경 이미지를 포함할 수 있습니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LiveRoomDTO> createRoom(@ModelAttribute CreateRoomRequestDTO req) {
        LiveRoomDTO room = liveRoomService.createRoom(req);
        return ResponseEntity.ok(room);
    }

    @Operation(summary = "방 조회",
            description = "특정 방의 정보를 조회합니다. 방 ID를 통해 방 정보를 가져옵니다.")
    @GetMapping("/{roomId}")
    public ResponseEntity<LiveRoomDTO> getRoom(@PathVariable Long roomId) {
        LiveRoomDTO room = liveRoomService.getRoom(roomId);
        return ResponseEntity.ok(room);
    }

    @Operation(summary = "방 삭제",
            description = "특정 방을 삭제합니다. 방 ID와 아티스트 ID를 통해 방 정보를 삭제합니다.")
    @DeleteMapping("/{roomId}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long roomId, @RequestParam Long artistId) {
        redisService.deleteRoomInfo(artistId, roomId);
        return ResponseEntity.ok("방이 삭제되었습니다.");
    }

    @Operation(summary ="방 입장",
            description = "특정 방을 입장합니다. 로그인한 유저, 로그인하지 않은 유저 모두 입장 가능합니다.\n"
                            + "잠겨있는 경우 에러 반환(입장질문 포함)하며 정답을 포함하여 재요청을 수행하면 됩니다.")
    @PostMapping("/{roomId}/enter")
    public ResponseEntity<LiveRoomDTO> enterRoom(
            @PathVariable Long roomId,
            @RequestParam(required = false) String entryAnswer,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        LiveRoomDTO room = redisService.getRoomInfo(roomId.toString());

        if (room == null) {
            throw new CustomException("존재하지 않는 방입니다", HttpStatus.NOT_FOUND);
        }

        if (room.isLocked()) {

            if (entryAnswer == null) {
                throw new CustomException(
                        "잠금 방입니다. 입장 질문에 대한 정답을 입력해야 합니다.",
                        HttpStatus.UNAUTHORIZED,
                        Map.of("entryQuestion", room.getEntryQuestion())
                );
            }

            if (!entryAnswer.equals(room.getEntryAnswer())) {
                throw new CustomException("정답이 일치하지 않습니다.", HttpStatus.UNAUTHORIZED);
            }
        }

        // 로그인 사용자인 경우 참여자 목록에 추가
        if (principal != null) {
            redisService.addUserToRoom(roomId.toString(), principal.getUser());
        }

        return ResponseEntity.ok(room);
    }

    @Operation(summary = "방 퇴장", description = "현재 로그인한 사용자가 방에서 퇴장합니다.")
    @PostMapping("/{roomId}/exit")
    public ResponseEntity<?> exitRoom(
            @PathVariable Long roomId,
            @RequestParam Long artistId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        if (principal == null) {
            throw new CustomException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }

        redisService.removeUserFromRoom(artistId.toString(), roomId.toString(), principal.getUser());

        return ResponseEntity.ok("방에서 퇴장하였습니다.");
    }



}
