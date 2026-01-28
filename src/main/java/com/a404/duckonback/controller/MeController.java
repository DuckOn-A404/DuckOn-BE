package com.a404.duckonback.controller;

import com.a404.duckonback.dto.*;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.filter.CustomUserPrincipal;
import com.a404.duckonback.response.ApiResponseDTO;
import com.a404.duckonback.response.ErrorCode;
import com.a404.duckonback.response.SuccessCode;
import com.a404.duckonback.service.ArtistFollowService;
import com.a404.duckonback.service.MemeService;
import com.a404.duckonback.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "내 계정", description = "로그인한 사용자 자신의 정보/설정 관리")
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

    private final UserService userService;
    private final ArtistFollowService artistFollowService;
    private final MemeService memeService;

    /**
     * 기본 정보
     */

    @Operation(summary = "내 정보 조회 (JWT 필요O)", description = "로그인한 사용자의 상세 정보를 조회합니다.")
    @GetMapping
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(userService.getUserDetailInfo(principal.getUser().getUserId()));
    }

    @Operation(summary = "내 정보 수정 (JWT 필요O)", description = "로그인한 사용자의 정보를 수정합니다.")
    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateMyInfo(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @ModelAttribute UpdateProfileRequestDTO newUserInfo
    ) {
        userService.updateUserInfo(principal.getUser().getUserId(), newUserInfo);
        return ResponseEntity.ok(Map.of("message", "회원 정보가 수정되었습니다."));
    }

    @Operation(summary = "회원 탈퇴 (JWT 필요O)", description = "로그인한 사용자의 계정을 삭제합니다.")
    @DeleteMapping
    public ResponseEntity<?> deleteUser(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestHeader("X-Refresh-Token") String refreshTokenHeader
    ) {

        userService.deleteUser(principal.getUser(), refreshTokenHeader);
        return ResponseEntity.ok(Map.of("message", "회원탈퇴가 완료되었습니다."));
    }

    @Operation(summary = "비밀번호 변경 (JWT 필요O)", description = "현재 비밀번호를 검증한 뒤 새 비밀번호로 변경합니다.")
    @PatchMapping("/password")
    public ResponseEntity<ApiResponseDTO<Void>> changePassword(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody PasswordChangeRequestDTO requestDTO
    ){
        userService.changePassword(principal.getId(), requestDTO);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.PASSWORD_CHANGE_SUCCESS));
    }

    @Operation(summary = "새로운 비밀번호 설정 (JWT 필요O)", description = "새 비밀번호로 변경합니다.")
    @PatchMapping("/new-password")
    public ResponseEntity<ApiResponseDTO<Void>> newPassword(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody String newPassword
    ){
        userService.setPassword(principal.getId(),newPassword);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.PASSWORD_CHANGE_SUCCESS));
    }

    @Operation(summary = "비밀번호 확인 (JWT 필요O)", description = "입력한 비밀번호가 현재 사용자의 비밀번호와 일치하는지 확인합니다.")
    @PostMapping("/verify-password")
    public ResponseEntity<?> verifyPassword(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody Map<String, String> request
    ) {
        String inputPassword = request.get("password");
        boolean isValid = userService.verifyPassword(principal.getUser().getUserId(), inputPassword);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }

    /**
     * 유저 팔로우
     */

    @Operation(summary = "내 팔로워 조회 (JWT 필요O)", description = "로그인한 사용자의 팔로워 목록을 조회합니다.")
    @GetMapping("/followers")
    public ResponseEntity<?> getFollowers(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(userService.getFollowers(principal.getUser().getUserId()));
    }

    @Operation(summary = "내 팔로잉 조회 (JWT 필요O)", description = "로그인한 사용자의 팔로잉 목록을 조회합니다.")
    @GetMapping("/following")
    public ResponseEntity<?> getFollowing(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(userService.getFollowing(principal.getUser().getUserId()));
    }

    @Operation(summary = "사용자 팔로우 (JWT 필요O)", description = "특정 사용자를 팔로우합니다.")
    @PostMapping("/following/{userId}")
    public ResponseEntity<?> followUser(@AuthenticationPrincipal CustomUserPrincipal principal, @PathVariable String userId) {
        userService.followUser(principal.getUser().getUserId(), userId);
        return ResponseEntity.ok(Map.of("message", "사용자를 팔로우했습니다."));
    }

    @Operation(summary = "사용자 언팔로우 (JWT 필요O)", description = "특정 사용자의 팔로우를 취소합니다.")
    @DeleteMapping("/following/{userId}")
    public ResponseEntity<?> unfollowUser(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable String userId
    ) {
        userService.unfollowUser(principal.getUser().getUserId(), userId);
        return ResponseEntity.ok(Map.of("message", "사용자 팔로우를 취소했습니다."));
    }

    /**
     * 아티스트
     */

    @Operation(summary = "내가 팔로우한 아티스트 조회 (JWT 필요O)",
            description = "로그인한 사용자가 팔로우한 아티스트 목록을 페이지 단위로 조회합니다.")
    @GetMapping("/artists")
    public ResponseEntity<?> getMyFollowedArtists(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        if (page < 1 || size < 1) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "잘못된 페이지 번호 또는 크기입니다."));
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<FollowedArtistDTO> dtoPage = artistFollowService.getFollowedArtists(
                principal.getUser().getId(), pageable);

        return ResponseEntity.ok(Map.of(
                "artistList", dtoPage.getContent(),
                "page", page,
                "size", size,
                "totalPages", dtoPage.getTotalPages(),
                "totalElements", dtoPage.getTotalElements()
        ));
    }

    /**
     * 밈
     */

    @Operation(
            summary = "내가 생성한 밈 수정 (JWT 필요O)",
            description = "본인이 생성한 밈만 수정할 수 있습니다. 현재는 태그 수정만 지원하며, 태그는 최소 1개, 최대 25개까지 가능합니다."
    )
    @PatchMapping("/{memeId}")
    public ResponseEntity<ApiResponseDTO<MemeDetailDTO>> updateMeme(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long memeId,
            @Valid @RequestBody MemeUpdateRequestDTO request
    ) {
        MemeDetailDTO result = memeService.updateMeme( principal.getId(), memeId, request);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.MEME_UPDATE_SUCCESS, result));
    }

    @Operation(
            summary = "내가 생성한 밈 삭제 (JWT 필요O)",
            description = "본인이 생성한 밈만 삭제할 수 있습니다."
    )
    @DeleteMapping("/{memeId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteMeme(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long memeId
    ) {
        if(principal == null) {
            throw new CustomException("로그인이 필요합니다.", ErrorCode.USER_NOT_AUTHENTICATED);
        }
        memeService.deleteMeme(principal.getId(), memeId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.MEME_DELETE_SUCCESS, null));
    }


    @Operation(
            summary = "내 즐겨찾기 밈 목록 조회 (JWT 필요O)",
            description = "내가 즐겨찾기한 밈을 최신순으로 조회합니다. 페이지네이션을 지원합니다."
    )
    @GetMapping("/favorite-memes")
    public ResponseEntity<ApiResponseDTO<MemeResponseDTO>> getMyFavoriteMemes(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        MemeResponseDTO favorites = memeService.getMyFavoriteMemes(principal.getId(), page, size);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.MEME_RETRIEVE_SUCCESS, favorites));
    }

}
