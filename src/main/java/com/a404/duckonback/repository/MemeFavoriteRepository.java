package com.a404.duckonback.repository;

import com.a404.duckonback.entity.MemeFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemeFavoriteRepository  extends JpaRepository<MemeFavorite, Long> {
    boolean existsByUser_IdAndMeme_Id(Long userId, Long memeId);
    void deleteByUser_IdAndMeme_Id(Long userId, Long memeId);

    @EntityGraph(attributePaths = {
            "meme",
            "meme.memeTags",
            "meme.memeTags.tag"
    })
    Page<MemeFavorite> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
