package com.a404.duckonback.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.a404.duckonback.dto.ImageDocument;
import com.a404.duckonback.dto.SearchResponseDto;
import com.a404.duckonback.service.SearchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@Slf4j
@Tag(name = "검색", description = "검색 기능을 제공합니다.")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "밈 검색", description = "밈을 검색합니다.")
    @GetMapping
    public ResponseEntity<SearchResponseDto> searchMemes(
        @RequestParam("q") String queryTerm,
        @RequestParam(value = "mode", defaultValue = "term") String mode) {

            try {
                log.info("🌐 [API] 밈 검색 요청 - queryTerm: {}, mode: {}", queryTerm, mode);
                SearchResponseDto response = searchService.searchByTag(queryTerm, mode);
                log.info("🌐 [API] 밈 검색 성공 - totalHits: {}, results: {}", response.getTotalHits(), response.getImages().size());
                return ResponseEntity.ok(response);
            } catch (IOException e) {
                log.error("❌ [API] 밈 검색 실패 - queryTerm: {}, mode: {}, error: {}", queryTerm, mode, e.getMessage(), e);
                return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== 🧪 테스트 엔드포인트 ====================

    @Operation(summary = "[테스트] OpenSearch 연결 확인", 
               description = "OpenSearch 클러스터 연결 상태를 확인합니다.")
    @GetMapping("/test/connection")
    public ResponseEntity<Map<String, Object>> testConnection() {
        try {
            log.info("🧪 [TEST] OpenSearch 연결 테스트 시작");
            Map<String, Object> result = searchService.testConnection();
            log.info("✅ [TEST] OpenSearch 연결 성공: {}", result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ [TEST] OpenSearch 연결 실패: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("status", "error");
            errorResult.put("message", e.getMessage());
            errorResult.put("type", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(errorResult);
        }
    }

    @Operation(summary = "[테스트] OpenSearch 수동 인덱싱", 
               description = "테스트 데이터를 OpenSearch에 직접 인덱싱합니다.")
    @GetMapping("/test/index")
    public ResponseEntity<Map<String, Object>> testIndexing() {
        try {
            log.info("🧪 [TEST] OpenSearch 인덱싱 테스트 시작");
            
            // 테스트 데이터 생성
            ImageDocument testDoc = ImageDocument.builder()
                .s3_url("https://test-cdn.cloudfront.net/test.gif")
                .object_key("test/test-" + System.currentTimeMillis() + ".gif")
                .tags(List.of("테스트", "test", "확인"))
                .created_at(LocalDateTime.now())
                .build();
            
            searchService.indexImage(testDoc);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "테스트 데이터 인덱싱 성공");
            result.put("indexed_document", testDoc);
            
            log.info("✅ [TEST] OpenSearch 인덱싱 성공: {}", testDoc.getObject_key());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ [TEST] OpenSearch 인덱싱 실패: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("status", "error");
            errorResult.put("message", e.getMessage());
            errorResult.put("type", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(errorResult);
        }
    }

    @Operation(summary = "[테스트] 전체 문서 조회", 
               description = "OpenSearch에 저장된 모든 문서를 조회합니다.")
    @GetMapping("/test/all")
    public ResponseEntity<Map<String, Object>> testGetAll() {
        try {
            log.info("🧪 [TEST] OpenSearch 전체 문서 조회 시작");
            Map<String, Object> result = searchService.getAllDocuments();
            log.info("✅ [TEST] OpenSearch 전체 문서 조회 성공: {} 건", result.get("total"));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ [TEST] OpenSearch 전체 문서 조회 실패: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("status", "error");
            errorResult.put("message", e.getMessage());
            errorResult.put("type", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(errorResult);
        }
    }
}

