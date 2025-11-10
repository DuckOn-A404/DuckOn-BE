package com.a404.duckonback.repository;

import com.a404.duckonback.entity.Meme;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface MemeRepository extends JpaRepository<Meme, Long> {
    @Query("""
        SELECT m
        FROM Meme m
        ORDER BY (m.usageCnt + m.downloadCnt) DESC, m.id ASC
    """)
    List<Meme> findTopByUsageAndDownload(Pageable pageable);
}
