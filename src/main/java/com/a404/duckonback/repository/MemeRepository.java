package com.a404.duckonback.repository;

import com.a404.duckonback.dto.MyMemeDTO;
import com.a404.duckonback.entity.Meme;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface MemeRepository extends JpaRepository<Meme, Long> {
    @Query("""
        SELECT m
        FROM Meme m
        ORDER BY (m.usageCnt + m.downloadCnt) DESC, m.id ASC
    """)
    List<Meme> findTopByUsageAndDownload(Pageable pageable);

    @Query("""
           select new com.a404.duckonback.dto.MyMemeDTO(
               m.id,
               m.imageUrl,
               m.createdAt,
               m.usageCnt,
               m.downloadCnt
           )
           from Meme m
           where m.creator.id = :creatorId
           order by m.createdAt desc
           """)
    Page<MyMemeDTO> findMyMemesByCreatorIdOrderByCreatedAtDesc(
            @Param("creatorId") Long creatorId,
            Pageable pageable
    );
}
