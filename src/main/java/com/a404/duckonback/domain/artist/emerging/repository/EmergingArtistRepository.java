package com.a404.duckonback.domain.artist.emerging.repository;

import com.a404.duckonback.domain.artist.emerging.dto.EmergingArtistDetailResponseDTO;
import com.a404.duckonback.domain.artist.emerging.entity.EmergingArtist;
import com.a404.duckonback.domain.artist.emerging.entity.EmergingArtistStatus;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmergingArtistRepository extends JpaRepository<EmergingArtist, Long>, EmergingArtistRepositoryCustom {
    boolean existsByNameKrAndNameEnAndStatus(String nameKr, String nameEn, EmergingArtistStatus status);

    @Query("""
        select ea
        from EmergingArtist ea
        join fetch ea.createdBy u
        where ea.emergingArtistId = :id
    """)
    Optional<EmergingArtist> findDetailById(@Param("id") Long id);

    @Query(value = """
        SELECT
                  ea.emerging_artist_id AS emergingArtistId,
                  ea.created_at         AS createdAt,
                  ea.debut_date         AS debutDate,
                  ea.name_kr            AS nameKr,
                  ea.name_en            AS nameEn,
                  ea.img_url            AS imgUrl,
                  ea.status             AS status,
                  (
                    SELECT COUNT(*)
                    FROM emerging_artist_follow f
                    WHERE f.emerging_artist_id = ea.emerging_artist_id
                  ) AS followerCount
                FROM emerging_artist ea
                WHERE ea.status = 'ACTIVE'
                ORDER BY RAND()
                LIMIT :count
        """, nativeQuery = true)
    List<EmergingArtistListView> findRandomActiveEmergingArtists(@Param("count") int count);

    interface EmergingArtistListView {
        Long getEmergingArtistId();
        java.time.LocalDateTime getCreatedAt();
        java.time.LocalDate getDebutDate();
        String getNameKr();
        String getNameEn();
        String getImgUrl();
        String getStatus(); // enum 변환은 service에서
        Long getFollowerCount();
    }
}
