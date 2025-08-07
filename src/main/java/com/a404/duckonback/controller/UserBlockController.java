package com.a404.duckonback.controller;

import com.a404.duckonback.dto.BlockedUserDTO;
import com.a404.duckonback.filter.CustomUserDetailsService;
import com.a404.duckonback.service.UserBlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "사용자 차단 관리", description = "사용자 차단, 차단 해제, 차단 여부 확인, 차단 목록 조회 기능을 제공합니다.")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserBlockController {

    private final UserBlockService userBlockService;

    /** 타 이용자 차단 */
    @Operation(
            summary = "사용자 차단",
            description = "특정 사용자를 차단합니다. 로그인한 사용자의 ID와 차단 대상자의 userId를 사용합니다."
    )
    @PostMapping("/block/{userId}")
    public ResponseEntity<?> blockUser(
            @PathVariable String userId,  // 차단 대상자의 userId(String)
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal principal
    ) {
        Long blockerId = principal.getUser().getId();  // 로그인한 사용자의 PK(Long)
        userBlockService.blockUser(blockerId, userId);
        return ResponseEntity.ok(Map.of("message", "사용자를 차단하였습니다."));
    }

    /** 차단 해제 */
    @Operation(
            summary = "차단 해제",
            description = "특정 사용자의 차단을 해제합니다. 로그인한 사용자의 ID와 차단 대상자의 userId를 사용합니다."
    )
    @DeleteMapping("/block/{userId}")
    public ResponseEntity<?> unblockUser(
            @PathVariable String userId,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal principal
    ) {
        Long blockerId = principal.getUser().getId();
        userBlockService.deleteUserBlock(blockerId, userId);
        return ResponseEntity.ok(Map.of("message", "차단을 해제하였습니다."));
    }

    /** 차단 여부 확인 */
    @Operation(
            summary = "차단 여부 확인",
            description = "특정 사용자가 차단되었는지 확인합니다. 로그인한 사용자의 ID와 차단 대상자의 userId를 사용합니다."
    )
    @GetMapping("/block/{userId}")
    public ResponseEntity<?> isUserBlocked(
            @PathVariable String userId,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal principal
    ) {
        Long blockerId = principal.getUser().getId();
        boolean isBlocked = userBlockService.isUserBlocked(blockerId, userId);
        return ResponseEntity.ok(Map.of("isBlocked", isBlocked));
    }

    /** 차단 목록 조회 */
    @Operation(
            summary = "차단 목록 조회",
            description = "로그인한 사용자가 차단한 사용자 목록을 조회합니다."
    )
    @GetMapping("/block")
    public ResponseEntity<?> getUserBlockList(
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal principal
    ) {
        Long blockerId = principal.getUser().getId();
        List<BlockedUserDTO> blockedList = userBlockService.getUserBlockList(blockerId);
        return ResponseEntity.ok(Map.of("blockedList", blockedList));
    }

}
