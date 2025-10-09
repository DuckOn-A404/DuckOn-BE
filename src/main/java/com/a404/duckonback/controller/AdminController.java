package com.a404.duckonback.controller;

//import com.a404.duckonback.dto.AdminArtistPatchDTO;
//import com.a404.duckonback.dto.AdminArtistRequestDTO;
import com.a404.duckonback.dto.AdminSubjectPatchDTO;
import com.a404.duckonback.dto.AdminSubjectRequestDTO;
//import com.a404.duckonback.service.ArtistService;
import com.a404.duckonback.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final SubjectService subjectService;

    @PostMapping("/subjects")
    public ResponseEntity<Map<String,String>> createSubject(
        @ModelAttribute @Valid AdminSubjectRequestDTO dto
    ) {
        subjectService.createSubject(dto);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(Map.of("message", "대상이 성공적으로 등록되었습니다."));
    }

    @PatchMapping("/subjects/{subjectId}")
    public ResponseEntity<Map<String,String>> patchSubject(
        @PathVariable Long subjectId,
        @ModelAttribute @Valid AdminSubjectPatchDTO dto
    ) {
        subjectService.patchSubject(subjectId, dto);
        return ResponseEntity.ok(Map.of("message", "대상 정보가 성공적으로 수정되었습니다."));
    }
}
