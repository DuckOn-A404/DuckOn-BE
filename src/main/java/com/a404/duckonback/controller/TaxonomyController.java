package com.a404.duckonback.controller;

import com.a404.duckonback.service.TaxonomyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "분류(Taxonomy) 공개 API")
@RestController
@RequestMapping("/api/taxonomy")
@RequiredArgsConstructor
public class TaxonomyController {
    private final TaxonomyService taxonomyService;

    @Operation(summary = "도메인 목록")
    @GetMapping("/domains")
    public ResponseEntity<?> getDomains() {
        return ResponseEntity.ok(Map.of("domains", taxonomyService.getDomains()));
    }

    @Operation(summary = "특정 도메인의 카테고리 트리")
    @GetMapping("/domains/{domainCode}/categories/tree")
    public ResponseEntity<?> getCategoryTree(@PathVariable String domainCode) {
        return ResponseEntity.ok(taxonomyService.getDomainTaxonomy(domainCode));
    }

    @Operation(summary = "특정 도메인의 카테고리 (플랫)")
    @GetMapping("/domains/{domainCode}/categories")
    public ResponseEntity<?> getCategoriesFlat(@PathVariable String domainCode) {
        return ResponseEntity.ok(Map.of("categories", taxonomyService.getFlatCategories(domainCode)));
    }
}

