package com.a404.duckonback.service;

import com.a404.duckonback.dto.CategoryDTO;
import com.a404.duckonback.dto.CategoryTreeDTO;
import com.a404.duckonback.dto.DomainDTO;
import com.a404.duckonback.dto.TaxonomyDTO;
import com.a404.duckonback.entity.Category;
import com.a404.duckonback.repository.CategoryRepository;
import com.a404.duckonback.repository.DomainRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaxonomyService {
    private final DomainRepository domainRepo;
    private final CategoryRepository categoryRepo;

    public List<DomainDTO> getDomains() {
        return domainRepo.findAll().stream()
            .map(d -> new DomainDTO(d.getDomainId(), d.getCode(), d.getName()))
            .toList();
    }

    public TaxonomyDTO getDomainTaxonomy(String domainCode) {
        var domain = domainRepo.findByCode(domainCode)
            .orElseThrow(() -> new IllegalArgumentException("domain not found: " + domainCode));

        var cats = categoryRepo.findAllByDomainIdOrderByDepthAsc(domain.getDomainId());
        // parentId -> children
        Map<Long, List<Category>> byParent = new HashMap<>();
        for (var c : cats) {
            Long p = c.getParent() == null ? null : c.getParent().getCategoryId();
            byParent.computeIfAbsent(p, k -> new ArrayList<>()).add(c);
        }

        // DFS 빌드
        java.util.function.Function<Category, CategoryTreeDTO> build = new java.util.function.Function<>() {
            @Override public CategoryTreeDTO apply(Category c) {
                var children = byParent.getOrDefault(c.getCategoryId(), java.util.List.of())
                    .stream().map(this::apply).toList();
                return new CategoryTreeDTO(
                    c.getCategoryId(), c.getCode(), c.getName(), c.getDepth(), children
                );
            }
        };

        var roots = byParent.getOrDefault(null, java.util.List.of()).stream()
            .map(build).toList();

        return new TaxonomyDTO(
            new DomainDTO(domain.getDomainId(), domain.getCode(), domain.getName()),
            roots
        );
    }

    public List<CategoryDTO> getFlatCategories(String domainCode) {
        return categoryRepo.findAllByDomainCodeOrderByDepthAsc(domainCode)
            .stream()
            .map(c -> new CategoryDTO(
                c.getCategoryId(), c.getCode(), c.getName(), c.getDepth(),
                c.getParent() == null ? null : c.getParent().getCategoryId()
            ))
            .toList();
    }
}
