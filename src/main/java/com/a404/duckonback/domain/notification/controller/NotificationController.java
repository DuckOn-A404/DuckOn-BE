package com.a404.duckonback.domain.notification.controller;

import com.a404.duckonback.common.dto.PageResponse;
import com.a404.duckonback.common.filter.CustomUserPrincipal;
import com.a404.duckonback.common.response.ApiResponseDTO;
import com.a404.duckonback.common.response.SuccessCode;
import com.a404.duckonback.domain.notification.dto.NotificationListDTO;
import com.a404.duckonback.domain.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
@Tag(name = "알림 관리", description = "사용자의 알림을 조회하는 기능을 제공합니다.")
public class NotificationController {
    private final NotificationService notificationService;

    @Operation(
            summary = "내 알림 조회",
            description = "로그인한 사용자가 자신의 알림 내역을 조회합니다. JWT 인증이 필요합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponseDTO<PageResponse<NotificationListDTO>>> getMyNotifications(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        PageResponse<NotificationListDTO> res = notificationService.getMyNotifications(principal.getId(), page, size);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.GET_MY_NOTIFICATION_LIST_SUCCESS, res));
    }

    @Operation(
            summary = "알림 읽음 처리",
            description = "로그인한 사용자가 특정 알림을 읽음 처리합니다. JWT 인증이 필요합니다."
    )
    @PatchMapping("/{notificationId}")
    public ResponseEntity<ApiResponseDTO<Void>> markNotificationAsRead(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long notificationId
    ){
        notificationService.markAsRead(principal.getId(), notificationId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.MARK_NOTIFICATION_AS_READ_SUCCESS));
    }
}
