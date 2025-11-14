package com.a404.duckonback.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import com.a404.duckonback.service.SearchService; 

import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.time.LocalDateTime;
import com.a404.duckonback.dto.ImageDocument;
import org.opensearch.client.opensearch.OpenSearchClient;

@Tag(name = "디버깅", description = "디버깅 기능을 제공합니다.")
@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {
    
    private final SearchService searchService;
    private final OpenSearchClient openSearchClient;

    @GetMapping("/opensearch-status")
    public ResponseEntity<Map<String, Object>> getOpenSearchStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // 연결 테스트
            var info = openSearchClient.info();
            status.put("connected", true);
            status.put("cluster_name", info.clusterName());
            status.put("version", info.version().number());
            
            // 인덱스 상태
            var exists = openSearchClient.indices().exists(e -> e.index("memes-index"));
            status.put("index_exists", exists.value());
            
            if (exists.value()) {
                var count = openSearchClient.count(c -> c.index("memes-index"));
                status.put("document_count", count.count());
            }
            
        } catch (Exception e) {
            status.put("connected", false);
            status.put("error", e.getMessage());
            status.put("error_class", e.getClass().getSimpleName());
        }
        
        return ResponseEntity.ok(status);
    }
    
    @PostMapping("/test-indexing")
    public ResponseEntity<Map<String, Object>> testIndexing() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            ImageDocument testDoc = ImageDocument.builder()
                    .s3_url("https://test.example.com/test-" + System.currentTimeMillis() + ".jpg")
                    .object_key("test-key-" + System.currentTimeMillis())
                    .tags(Arrays.asList("test", "debug"))
                    .created_at(LocalDateTime.now())
                    .build();
            
            result.put("test_document", testDoc);
            
            searchService.indexImage(testDoc);
            
            result.put("indexing_success", true);
            result.put("message", "테스트 문서 인덱싱 성공");
            
            // 바로 검증
            Thread.sleep(1000);
            var searchResult = searchService.searchByTag("test", "term");
            result.put("search_verification", searchResult.getTotalHits() > 0);
            result.put("search_hits", searchResult.getTotalHits());
            
        } catch (Exception e) {
            result.put("indexing_success", false);
            result.put("error", e.getMessage());
            result.put("error_class", e.getClass().getSimpleName());
        }
        
        return ResponseEntity.ok(result);
    }
}