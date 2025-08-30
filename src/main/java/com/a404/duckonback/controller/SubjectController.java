package com.a404.duckonback.controller;

import com.a404.duckonback.dto.SubjectDTO;
import com.a404.duckonback.dto.SubjectDetailDTO;
import com.a404.duckonback.dto.FollowedSubjectDTO;
import com.a404.duckonback.dto.UpdateSubjectFollowRequestDTO;
import com.a404.duckonback.filter.CustomUserPrincipal;
import com.a404.duckonback.service.SubjectFollowService;
import com.a404.duckonback.service.SubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "주체(Subject) 관리", description = "Subject 정보 조회, 팔로우/언팔로우, 검색 등의 기능을 제공합니다.")
@Slf4j
@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;
    private final SubjectFollowService subjectFollowService;

    // 단일 Subject 상세 조회
    @Operation(summary = "Subject 상세 조회",
        description = "특정 Subject의 상세 정보를 조회합니다. 로그인한 사용자의 팔로우 상태도 함께 반환됩니다.")
    @GetMapping("/{subjectId}")
    public ResponseEntity<?> getSubject(
        @PathVariable Long subjectId,
        @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Long userId = principal != null ? principal.getUser().getId() : null;
        SubjectDetailDTO dto = subjectService.getSubjectDetail(userId, subjectId);
        return ResponseEntity.ok(dto);
    }

    // 전체 Subject 목록/검색/정렬 통합
    @Operation(summary = "Subject 목록/검색/정렬 조회",
        description = "페이지네이션 + 정렬(followers/name/debut) + 검색(keyword)을 지원합니다.")
    @GetMapping
    public ResponseEntity<?> getSubjectList(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "30") int size,
        @RequestParam(defaultValue = "followers") String sort,
        @RequestParam(defaultValue = "desc") String order,
        @RequestParam(required = false) String keyword
    ) {
        if (page < 1 || size < 1) {
            return ResponseEntity.badRequest().body(Map.of("message", "잘못된 페이지 번호 또는 크기입니다."));
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<SubjectDTO> dtoPage = subjectService.getSubjects(pageable, sort, order, keyword);

        return ResponseEntity.ok(Map.of(
            "subjectList", dtoPage.getContent(),
            "page", page,
            "size", size,
            "totalPages", dtoPage.getTotalPages(),
            "totalElements", dtoPage.getTotalElements()
        ));
    }

    // 키워드 검색 (단순 리스트)
    @Operation(summary = "Subject 검색",
        description = "Subject 이름(다국어) 키워드로 검색합니다. 페이지 없이 리스트로 반환됩니다.")
    @GetMapping(params = {"keyword", "!page", "!size", "!sort", "!order"})
    public ResponseEntity<?> searchSubjects(@RequestParam String keyword) {
        var list = subjectService.searchSubjects(keyword);
        return ResponseEntity.ok(Map.of("subjectList", list));
    }

    // 랜덤 Subject 조회
    @Operation(summary = "랜덤 Subject 조회",
        description = "지정된 크기만큼 랜덤으로 Subject를 조회합니다. 기본 크기는 16입니다.")
    @GetMapping("/random")
    public ResponseEntity<?> getRandomSubjects(@RequestParam(defaultValue = "16") int size) {
        var list = subjectService.getRandomSubjects(size);
        return ResponseEntity.ok(Map.of("subjectList", list));
    }

    // 내가 팔로우한 Subject 조회
    @Operation(summary = "내가 팔로우한 Subject 조회",
        description = "로그인한 사용자가 팔로우한 Subject 목록을 페이지 단위로 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<?> getMyFollowedSubjects(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @AuthenticationPrincipal CustomUserPrincipal principal) {

        if (page < 1 || size < 1) {
            return ResponseEntity.badRequest().body(Map.of("message", "잘못된 페이지 번호 또는 크기입니다."));
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<FollowedSubjectDTO> dtoPage = subjectFollowService.getFollowedSubjects(
            principal.getUser().getId(), pageable);

        return ResponseEntity.ok(Map.of(
            "subjectList", dtoPage.getContent(),
            "page", page,
            "size", size,
            "totalPages", dtoPage.getTotalPages(),
            "totalElements", dtoPage.getTotalElements()
        ));
    }

    // Subject 팔로우
    @Operation(summary = "Subject 팔로우",
        description = "로그인한 사용자가 특정 Subject를 팔로우합니다.")
    @PostMapping("/{subjectId}/follow")
    public ResponseEntity<?> followSubject(
        @PathVariable Long subjectId,
        @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Long userId = principal.getUser().getId();
        subjectFollowService.followSubject(userId, subjectId);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(Map.of("message", "대상을 팔로우했습니다."));
    }

    // Subject 팔로우 취소
    @Operation(summary = "Subject 팔로우 취소",
        description = "로그인한 사용자가 특정 Subject의 팔로우를 취소합니다.")
    @DeleteMapping("/{subjectId}/follow")
    public ResponseEntity<?> unfollowSubject(
        @PathVariable Long subjectId,
        @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Long userId = principal.getUser().getId();
        subjectFollowService.unfollowSubject(userId, subjectId);
        return ResponseEntity.ok(Map.of("message", "대상 팔로우를 취소했습니다."));
    }

    // Subject 팔로우 목록 일괄 수정
    @Operation(summary = "Subject 팔로우 목록 수정",
        description = "로그인한 사용자의 Subject 팔로우 목록을 요청 본문대로 일괄 재설정합니다.")
    @PutMapping("/follow")
    public ResponseEntity<?> updateFollows(
        @RequestBody UpdateSubjectFollowRequestDTO req,
        @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Long userId = principal.getUser().getId();
        subjectFollowService.updateSubjectFollows(userId, req.getSubjectList());
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(Map.of("message", "Subject 팔로우 목록을 수정했습니다."));
    }
}
