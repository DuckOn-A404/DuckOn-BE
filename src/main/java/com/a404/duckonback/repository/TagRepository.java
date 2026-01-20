package com.a404.duckonback.repository;

import com.a404.duckonback.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByTagName(String tagName);
    List<Tag> findByTagNameStartingWithIgnoreCase(String prefix, Pageable pageable);    
    List<Tag> findByTagNameContainingIgnoreCase(String keyword, Pageable pageable);
}
