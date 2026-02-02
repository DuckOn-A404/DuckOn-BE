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

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Validated
public class ReportController {
    private final ReportService reportService;
    // todo: 신고 생성
    @PostMapping("/create")
    public ResponseEntity<ApiResponseDTO<Void>> createReport(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @RequestBody ReportCreateRequestDTO request
    ) {
        reportService.createReport(request, principal.getUser());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.REPORT_CREATE_SUCCESS));
    }
    // todo: 같은 사용자가 동일 컨텐츠 중복 신고 안되게
    // todo: 자기 자신 신고 방지
    // todo: contentId 유효성 검사

}
