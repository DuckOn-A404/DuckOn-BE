package com.a404.duckonback.controller;

import com.a404.duckonback.dto.AdminArtistPatchDTO;
import com.a404.duckonback.dto.AdminArtistRequestDTO;
import com.a404.duckonback.response.ApiResponseDTO;
import com.a404.duckonback.response.SuccessCode;
import com.a404.duckonback.service.ArtistService;
import com.a404.duckonback.service.EngagementBatchService;
import com.a404.duckonback.service.MemeRankingBatchService;
import com.a404.duckonback.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.a404.duckonback.dto.ReportDTO;
import java.util.List;
import java.util.Map;

@Tag(name = "관리자", description = "관리자 전용 API")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final ArtistService artistService;
    private final EngagementBatchService engagementBatchService;
    private final MemeRankingBatchService memeRankingBatchService;
    private final ReportService reportService;

    @Operation(summary = "아티스트 등록 (JWT 필요O)", description = "새로운 아티스트를 등록합니다.")
    @PostMapping("/artists")
    public ResponseEntity<Map<String,String>> createArtist(
            @ModelAttribute @Valid AdminArtistRequestDTO dto
    ) {
        artistService.createArtist(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("message", "아티스트가 성공적으로 등록되었습니다."));
    }

    @Operation(summary = "아티스트 정보 수정 (JWT 필요O)", description = "기존 아티스트의 정보를 수정합니다.")
    @PatchMapping("/artists/{artistId}")
    public ResponseEntity<Map<String,String>> patchArtist(
            @PathVariable Long artistId,
            @ModelAttribute @Valid AdminArtistPatchDTO dto
    ) {
        artistService.patchArtist(artistId, dto);
        return ResponseEntity.ok(Map.of("message", "아티스트 정보가 성공적으로 수정되었습니다."));
    }

    @Operation(summary = "유저 참여도 지표 재생성 (JWT 필요O)", description = "유저 참여도 지표 스냅샷을 재생성합니다.")
    @PostMapping("/batch/engagement/rebuild")
    public ResponseEntity<ApiResponseDTO> rebuildEngagement() {
        engagementBatchService.rebuildEngagementSnapshot();
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.ADMIN_REBUILD_ENGAGEMENT_SUCCESS));
    }

    @Operation(
            summary = "시간별 밈 TOP10 집계(직전 1시간) 수동 실행 (JWT 필요O)",
            description = "meme_usage_log 기반으로 직전 1시간 구간의 밈 사용/다운로드 로그를 집계하여 meme_hourly_top10에 저장합니다."
    )
    @PostMapping("/batch/meme/hourly-top10")
    public ResponseEntity<ApiResponseDTO<Void>> runMemeHourlyTop10Batch() {
        memeRankingBatchService.aggregateHourlyTopMemes();
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.ADMIN_BUILD_MEME_HOURLY_TOP10_SUCCESS));
    }

    @Operation(summary = "신고 목록 조회 (JWT 필요O)", description = "신고 목록을 조회합니다.")                                                                                       
    @GetMapping("/reports")                                                                                                                                                           
    public ResponseEntity<ApiResponseDTO<List<ReportDTO>>> getAllReports() {                                                                                                          
        List<ReportDTO> reportDTOs = reportService.getAllReports()                                                                                                                    
                .stream()                                                                                                                                                             
                .map(ReportDTO::fromEntity)                                                                                                                                           
                .toList();                                                                                                                                                            
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.ADMIN_GET_REPORT_LIST_SUCCESS, reportDTOs));                                                                      
    }       
}
