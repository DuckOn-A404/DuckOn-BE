package com.a404.duckonback.domain.report.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import com.a404.duckonback.domain.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import com.a404.duckonback.common.response.ApiResponseDTO;
import com.a404.duckonback.common.response.SuccessCode;
import com.a404.duckonback.domain.report.dto.ReportCreateRequestDTO;
import com.a404.duckonback.common.filter.CustomUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "신고 관리", description = "신고 생성, 조회, 삭제 등의 기능을 제공합니다.")
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Validated
public class ReportController {
    private final ReportService reportService;
    
    @Operation(summary = "신고 생성 (JWT 필요O)", description = "신고를 생성합니다.")
    @PostMapping("/create")
    public ResponseEntity<ApiResponseDTO<Void>> createReport(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @RequestBody ReportCreateRequestDTO request
    ) {
        reportService.createReport(request, principal.getUser());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.REPORT_CREATE_SUCCESS));
    }

}
