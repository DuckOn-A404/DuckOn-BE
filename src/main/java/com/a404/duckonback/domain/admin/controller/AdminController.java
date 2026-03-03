package com.a404.duckonback.domain.admin.controller;

import com.a404.duckonback.common.dto.PageResponse;
import com.a404.duckonback.common.enums.ReportStatus;
import com.a404.duckonback.common.enums.ReportType;
import com.a404.duckonback.common.enums.UserRole;
import com.a404.duckonback.common.response.ApiResponseDTO;
import com.a404.duckonback.common.response.SuccessCode;
import com.a404.duckonback.domain.admin.dto.*;
import com.a404.duckonback.domain.admin.service.AdminService;
import com.a404.duckonback.domain.artist.artist.service.ArtistService;
import com.a404.duckonback.domain.artist.emerging.service.EmergingArtistService;
import com.a404.duckonback.domain.home.dto.HomeSearchPlaceholderResponseDTO;
import com.a404.duckonback.domain.home.dto.HomeSearchPlaceholderUpdateRequestDTO;
import com.a404.duckonback.domain.artist.artist.dto.ArtistDTO;
import com.a404.duckonback.domain.home.service.HomeSearchPlaceholderService;
import com.a404.duckonback.domain.meme.service.MemeRankingBatchService;
import com.a404.duckonback.domain.report.dto.ReportDTO;
import com.a404.duckonback.domain.admin.dto.ReportSearchConditionDTO;
import com.a404.duckonback.domain.report.service.ReportService;
import com.a404.duckonback.domain.user.service.EngagementBatchService;
import com.a404.duckonback.common.filter.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;

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
    private final AdminService adminService;
    private final ReportService reportService;
    private final EmergingArtistService emergingArtistService;
    private final HomeSearchPlaceholderService homeSearchPlaceholderService;

    @Operation(summary = "아티스트 등록 (JWT 필요O)", description = "새로운 아티스트를 등록합니다.")
    @PostMapping("/artists")
    public ResponseEntity<Map<String,String>> createArtist(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody @Valid AdminArtistCreateRequestDTO dto
    ) {
        Long userId = principal.getId();
        artistService.createArtist(userId, dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("message", "아티스트가 성공적으로 등록되었습니다."));
    }

    @Operation(summary = "아티스트 정보 수정 (JWT 필요O)", description = "기존 아티스트의 정보를 수정합니다.")
    @PatchMapping("/artists/{artistId}")
    public ResponseEntity<ApiResponseDTO<Void>> patchArtist(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long artistId,
            @ModelAttribute @Valid AdminArtistPatchDTO dto
    ) {
        artistService.patchArtist(principal.getId(), artistId, dto);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.ADMIN_PATCH_ARTIST_SUCCESS));
    }

    @Operation(summary = "라이징 아티스트 정보 수정 (JWT 필요O)", description = "기존 라이징 아티스트의 정보를 수정합니다.")
    @PatchMapping("/emerging-artists/{emergingArtistId}")
    public ResponseEntity<ApiResponseDTO<Void>> updateEmergingArtist(
            @PathVariable Long emergingArtistId,
            @RequestBody @Valid AdminEmergingArtistUpdateRequestDTO dto
    ) {
        emergingArtistService.updateEmergingArtist(emergingArtistId, dto);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.ADMIN_PATCH_EMERGING_ARTIST_SUCCESS));
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

    @Operation(summary = "유저 검색 (JWT 필요O)", description = "키워드와 역할로 유저를 검색합니다.")
    @GetMapping("/users/search")
    public ResponseEntity<ApiResponseDTO<PageResponse<AdminUserListDTO>>> searchUsers(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) UserRole role,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        UserSearchConditionDTO condition = new UserSearchConditionDTO();
        condition.setKeyword(keyword);
        condition.setRole(role);
        
        PageResponse<AdminUserListDTO> result = adminService.searchAdminUserList(condition, page, size);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.ADMIN_SEARCH_USER_SUCCESS, result));
    }

    @Operation(summary = "관리자 유저 리스트 조회 (JWT 필요O)", description = "관리자용 유저 리스트를 페이징 처리하여 조회합니다.")
    @GetMapping("/users")
    public ResponseEntity<ApiResponseDTO<PageResponse<AdminUserListDTO>>> getUserList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<AdminUserListDTO> userList = adminService.getAdminUserList(page, size);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.ADMIN_GET_USER_LIST_SUCCESS, userList));
    }

    @Operation(summary = "유저 디테일 조회 (JWT 필요O)", description = "유저 디테일을 조회합니다. - 관리자.ver")
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponseDTO<AdminUserDetailDTO>> getUserDetail(@PathVariable String userId) {
        AdminUserDetailDTO userDetail = adminService.getUserDetail(userId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.ADMIN_GET_USER_DETAIL_SUCCESS, userDetail));
    }

    @Operation(summary = "신고 다중 필터 검색", description = "신고자, 피신고자, 상태, 유형으로 필터링합니다.")
    @GetMapping("/reports/search")
    public ResponseEntity<ApiResponseDTO<PageResponse<ReportDTO>>> searchReports(
        @RequestParam(required = false) String reporterUserId,
        @RequestParam(required = false) String reportedUserId,
        @RequestParam(required = false) ReportStatus status,
        @RequestParam(required = false) ReportType type,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        ReportSearchConditionDTO condition = new ReportSearchConditionDTO();
        condition.setReporterUserId(reporterUserId);
        condition.setReportedUserId(reportedUserId);
        condition.setStatus(status);
        condition.setType(type);
        
        PageResponse<ReportDTO> result = reportService.searchReports(condition, page, size);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.REPORT_SEARCH_SUCCESS, result));
    }

    @Operation(summary = "신고 목록 조회 (JWT 필요O)", description = "신고 목록을 조회합니다.")                                                                                       
    @GetMapping("/reports")                                                                                                                                                           
    public ResponseEntity<ApiResponseDTO<PageResponse<ReportDTO>>> getAllReports(
        @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {                                                                                
        PageResponse<ReportDTO> reportDTOs = reportService.getAllReports(page, size);                                                                                                                                                                                                                                                                             
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.ADMIN_GET_REPORT_LIST_SUCCESS, reportDTOs));                                                                      
    }     

    @Operation(summary = "신고 상세 조회(JWT 필요O)", description = "신고 상세를 조회합니다.")
    @GetMapping("/reports/{reportId}")
    public ResponseEntity<ApiResponseDTO<ReportDTO>> getReportDetail(@PathVariable Long reportId) {
        ReportDTO reportDTO = reportService.getReportDetail(reportId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.ADMIN_GET_REPORT_DETAIL_SUCCESS, reportDTO));
    }

    @Operation(summary = "아티스트 조회(JWT 필요O)", description = "전체 아티스트를 조회합니다.")
    @GetMapping("/artists")
    public ResponseEntity<ApiResponseDTO<PageResponse<AdminArtistListDTO>>> getAllArtists(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<AdminArtistListDTO> artistDTOs = adminService.getAllArtists(page, size);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.ADMIN_GET_ARTIST_LIST_SUCCESS, artistDTOs));
    }

    @Operation(summary = "아티스트 검색 (JWT 필요O)", description = "아티스트 이름이나 설명을 키워드로 검색합니다.")
    @GetMapping("/artists/search")
    public ResponseEntity<ApiResponseDTO<List<ArtistDTO>>> searchArtists(
        @RequestParam String keyword
    ) {
        List<ArtistDTO> artists = artistService.searchArtists(keyword);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.ADMIN_SEARCH_ARTIST_SUCCESS, artists));
    }

    @Operation(summary = "아티스트 삭제(JWT 필요O)", description = "아티스트를 삭제합니다.")
    @DeleteMapping("/artists/{artistId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteArtist(@PathVariable Long artistId) {
        artistService.deleteArtist(artistId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.ADMIN_DELETE_ARTIST_SUCCESS));
    }

    @Operation(
            summary = "홈 검색창 플레이스홀더 수정 (JWT 필요O)",
            description = "관리자가 홈 검색창에 표시될 플레이스홀더 목록을 전체 교체합니다."
    )
    @PutMapping("/home/search-placeholder")
    public ResponseEntity<ApiResponseDTO<HomeSearchPlaceholderResponseDTO>> updateHomeSearchPlaceholder(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody @Valid HomeSearchPlaceholderUpdateRequestDTO request
    ) {
        HomeSearchPlaceholderResponseDTO response = homeSearchPlaceholderService.updatePlaceholders(request.getItems(), principal.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.ADMIN_UPDATE_HOME_SEARCH_PLACEHOLDER_SUCCESS, response));
    }

}
