package com.a404.duckonback.repository;

import com.a404.duckonback.entity.MemeFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemeFavoriteRepository  extends JpaRepository<MemeFavorite, Long> {
    boolean existsByUser_IdAndMeme_Id(Long userId, Long memeId);
    void deleteByUser_IdAndMeme_Id(Long userId, Long memeId);
}
