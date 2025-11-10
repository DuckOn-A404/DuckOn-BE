package com.a404.duckonback.controller;

import com.a404.duckonback.dto.*;
import com.a404.duckonback.filter.CustomUserPrincipal;
import com.a404.duckonback.response.ApiResponseDTO;
import com.a404.duckonback.response.SuccessCode;
import com.a404.duckonback.service.MemeS3Service;
import com.a404.duckonback.service.MemeService;
import com.a404.duckonback.service.MemeUsageLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "밈 관리", description = "MemeDuck에서 사용되는 밈과 관련된 기능을 제공합니다.")
@RestController
@RequestMapping("/api/memes")
@RequiredArgsConstructor
public class MemeController {

    private final MemeService memeService;
    private final MemeS3Service memeS3Service;
    private final MemeUsageLogService memeUsageLogService;

    @Operation(
            summary = "밈 생성(DB까지 저장)",
            description = "새로운 밈을 등록합니다. 최대 3개의 밈을 업로드할 수 있으며 각 밈마다 태그를 지정할 수 있습니다." +
                    "‼️스웨거에서 'Send empty value'를 체크하면 에러가 납니다. 체크를 해제하고 빈값을 보내주세요‼️"
    )
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDTO<MemeCreateResponseDTO>> create(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @ModelAttribute MemeCreateRequestDTO request
    ) {
        Long userId = principal.getId();
        MemeCreateResponseDTO res = memeService.createMeme(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(SuccessCode.MEME_UPLOAD_SUCCESS, res));
    }

    @Operation(
            summary = "밈 S3에 업로드(테스트용)",
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
            summary = "랜덤 밈 조회",
            description = "랜덤으로 밈을 조회합니다. 페이지네이션을 지원합니다."
    )
    @GetMapping("/random")
    public ResponseEntity<ApiResponseDTO<RandomMemeResponseDTO>> getRandomMeme(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        RandomMemeResponseDTO randomMemes = memeService.getRandomMemes(page, size);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.MEME_RETRIEVE_SUCCESS, randomMemes));
    }

    @Operation(
            summary = "밈 사용/다운로드 로그 기록",
            description = "밈 사용(채팅에서 사용) 또는 다운로드 시 호출하여 로그와 집계 카운트를 기록합니다."
    )
    @PostMapping("/usage")
    public ResponseEntity<ApiResponseDTO<Void>> logMemeUsage(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody MemeUsageLogRequestDTO request
    ) {
        Long userId = principal.getId();
        memeUsageLogService.logMemeUsage(userId, request.getMemeId(), request.getUsageType());

        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.MEME_USAGE_LOG_SUCCESS, null));
    }

}
