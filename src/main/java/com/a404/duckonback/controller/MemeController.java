package com.a404.duckonback.controller;

import com.a404.duckonback.dto.*;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.filter.CustomUserPrincipal;
import com.a404.duckonback.response.ApiResponseDTO;
import com.a404.duckonback.response.ErrorCode;
import com.a404.duckonback.response.SuccessCode;
import com.a404.duckonback.service.MemeS3Service;
import com.a404.duckonback.service.MemeService;
import com.a404.duckonback.service.MemeUsageLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashSet;
import java.util.List;

@Tag(name = "밈 관리", description = "MemeDuck에서 사용되는 밈과 관련된 기능을 제공합니다.")
@RestController
@RequestMapping("/api/memes")
@RequiredArgsConstructor
public class MemeController {

    private final MemeService memeService;
    private final MemeS3Service memeS3Service;
    private final MemeUsageLogService memeUsageLogService;

    @Operation(
            summary = "밈 생성 (JWT 필요O)",
            description = "최대 3개의 이미지를 업로드하여 밈을 생성합니다. 각 이미지에 태그를 지정할 수 있습니다."
    )
    @PostMapping("/create")
    public ResponseEntity<ApiResponseDTO<MemeCreateResponseDTO>> createMemes(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam("image1") MultipartFile image1,
            @RequestParam(value = "image2", required = false) MultipartFile image2,
            @RequestParam(value = "image3", required = false) MultipartFile image3,
            @RequestParam("tags1") List<String> tags1,
            @RequestParam(value = "tags2", required = false) List<String> tags2,
            @RequestParam(value = "tags3", required = false) List<String> tags3
    ) {
        if (principal == null) {
            throw new CustomException("로그인이 필요합니다.", ErrorCode.USER_NOT_AUTHENTICATED);
        }

        MemeCreateRequestDTO req = MemeCreateRequestDTO.builder()
                .image1(image1)
                .image2(image2)
                .image3(image3)
                .tags1(tags1 != null ? new LinkedHashSet<>(tags1) : null)
                .tags2(tags2 != null ? new LinkedHashSet<>(tags2) : null)
                .tags3(tags3 != null ? new LinkedHashSet<>(tags3) : null)
                .build();

        MemeCreateResponseDTO res = memeService.createMemes(principal.getId(), req);

        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.MEME_UPLOAD_SUCCESS, res));
    }

    @Operation(
            summary = "밈 S3에 업로드(테스트용) (JWT 필요O)",
            description = "파일을 S3에 업로드합니다."
    )
    @PostMapping(
            value = "/upload-s3-only",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponseDTO<MemeS3UploadResponseDTO>> uploadToS3(
            @RequestPart("file") MultipartFile file
    ) {
        MemeS3UploadResponseDTO result = memeS3Service.uploadMeme(file);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.FILE_S3_UPLOAD_SUCCESS, result));
    }

    @Operation(
            summary = "랜덤 밈 조회 (JWT 필요X)",
            description = "랜덤으로 밈을 조회합니다. 페이지네이션을 지원합니다."
    )
    @GetMapping("/random")
    public ResponseEntity<ApiResponseDTO<MemeResponseDTO>> getRandomMeme(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        MemeResponseDTO randomMemes = memeService.getRandomMemes(page, size);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.MEME_RETRIEVE_SUCCESS, randomMemes));
    }

    @Operation(
            summary = "밈 사용/다운로드 로그 기록 (JWT 필요X)",
            description = "밈 사용(채팅에서 사용) 또는 다운로드 시 호출하여 로그와 집계 카운트를 기록합니다."
    )
    @PostMapping("/usage")
    public ResponseEntity<ApiResponseDTO<Void>> logMemeUsage(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody MemeUsageLogRequestDTO request
    ) {
        memeUsageLogService.logMemeUsage(principal, request.getMemeId(), request.getUsageType());

        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.MEME_USAGE_LOG_SUCCESS, null));
    }

    @Operation(summary = "밈 즐겨찾기 추가 (JWT 필요O)", description = "특정 밈을 즐겨찾기에 추가합니다. 이미 추가되어 있어도 에러 없이 성공 처리합니다(idempotent).")
    @PostMapping("/{memeId}/favorite")
    public ResponseEntity<ApiResponseDTO<Void>> createFavorite(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long memeId
    ) {
        if(principal == null) throw new CustomException("로그인이 필요합니다.", ErrorCode.USER_NOT_AUTHENTICATED);

        memeService.createFavorite(principal.getId(), memeId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.MEME_FAVORITE_CREATED, null));
    }

    @Operation(summary = "밈 즐겨찾기 취소 (JWT 필요O)", description = "특정 밈의 즐겨찾기를 취소합니다. 존재하지 않아도 에러 없이 성공 처리합니다(idempotent).")
    @DeleteMapping("/{memeId}/favorite")
    public ResponseEntity<ApiResponseDTO<Void>> deleteFavorite(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long memeId
    ) {
        if(principal == null) throw new CustomException("로그인이 필요합니다.", ErrorCode.USER_NOT_AUTHENTICATED);

        memeService.deleteFavorite(principal.getId(), memeId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.MEME_FAVORITE_DELETED, null));
    }

    @Operation(
            summary = "시간별 TOP10 밈 조회 (JWT 필요X)",
            description = "가장 최근 집계된 1시간 구간 기준 TOP10 밈을 반환합니다."
    )
    @GetMapping("/top/hourly")
    public ResponseEntity<ApiResponseDTO<MemeResponseDTO>> getHourlyTop10Memes() {
        MemeResponseDTO res = memeService.getHourlyTop10Memes();
        return ResponseEntity.ok(
                ApiResponseDTO.success(SuccessCode.MEME_TOP10_RETRIEVE_SUCCESS, res)
        );
    }

    @Operation(
            summary = "누적 TOP10 밈 조회 (JWT 필요X)",
            description = "meme 테이블의 usageCnt + downloadCnt 누적 합 기준으로 상위 10개의 밈을 반환합니다."
    )
    @GetMapping("/top/total")
    public ResponseEntity<ApiResponseDTO<MemeResponseDTO>> getTop10MemesByTotalUsage() {
        MemeResponseDTO res = memeService.getTop10MemesByTotalUsage();
        return ResponseEntity.ok(
                ApiResponseDTO.success(SuccessCode.MEME_TOP10_RETRIEVE_SUCCESS, res)
        );
    }

    @Operation(
            summary = "밈 상세 조회 (JWT 필요X)",
            description = "밈 기본정보, 태그 이름 리스트, 생성자 정보를 반환합니다."
    )
    @GetMapping("/{memeId}/detail")
    public ResponseEntity<ApiResponseDTO<MemeDetailDTO>> getMemeDetail(
            @PathVariable Long memeId
    ) {
        MemeDetailDTO detail = memeService.getMemeDetail(memeId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.MEME_RETRIEVE_SUCCESS, detail));
    }

    @Operation(
            summary = "태그 기반 밈 검색 (JWT 필요X)",
            description = "입력한 키워드를 포함(부분 일치)하는 태그명을 가진 밈들을 조회합니다. 결과는 usageCnt 내림차순으로 정렬됩니다."
    )
    @GetMapping("/search-basic")
    public ResponseEntity<ApiResponseDTO<MemeResponseDTO>> searchMemesByTagBasic(
            @RequestParam String tag,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        MemeResponseDTO searchedMemes = memeService.searchByTagBasic(tag, page, size);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.MEME_RETRIEVE_SUCCESS, searchedMemes));
    }

}
