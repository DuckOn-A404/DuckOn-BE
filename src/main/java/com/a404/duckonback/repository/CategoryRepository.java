package com.a404.duckonback.repository;

import com.a404.duckonback.entity.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("select c from Category c where c.domain.domainId = :domainId order by c.depth asc, c.name asc")
    List<Category> findAllByDomainIdOrderByDepthAsc(Long domainId);

    @Query("select c from Category c where c.domain.code = :domainCode order by c.depth asc, c.name asc")
    List<Category> findAllByDomainCodeOrderByDepthAsc(String domainCode);
}