package com.a404.duckonback.controller;

import com.a404.duckonback.oauth.principal.CustomUserPrincipal;
import com.a404.duckonback.service.UserBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserBlockController {

    private final UserBlockService userBlockService;

    /** 타 이용자 차단 */
    @PostMapping("/block/{userId}")
    public ResponseEntity<?> blockUser(
            @PathVariable String userId,  // 차단 대상자의 userId(String)
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Long blockerId = principal.getUser().getId();  // 로그인한 사용자의 PK(Long)
        userBlockService.blockUser(blockerId, userId);
        return ResponseEntity.ok(Map.of("message", "사용자를 차단하였습니다."));
    }

    /** 차단 해제 */
    @DeleteMapping("/block/{userId}")
    public ResponseEntity<?> unblockUser(
            @PathVariable String userId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Long blockerId = principal.getUser().getId();
        userBlockService.deleteUserBlock(blockerId, userId);
        return ResponseEntity.ok(Map.of("message", "차단을 해제하였습니다."));
    }

    /** 차단 여부 확인 */
    @GetMapping("/block/{userId}")
    public ResponseEntity<?> isUserBlocked(
            @PathVariable String userId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Long blockerId = principal.getUser().getId();
        boolean isBlocked = userBlockService.isUserBlocked(blockerId, userId);
        return ResponseEntity.ok(Map.of("isBlocked", isBlocked));
    }

}
