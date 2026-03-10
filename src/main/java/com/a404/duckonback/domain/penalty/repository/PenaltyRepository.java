package com.a404.duckonback.domain.penalty.repository;

import com.a404.duckonback.common.enums.PenaltyStatus;
import com.a404.duckonback.common.enums.PenaltyType;
import com.a404.duckonback.domain.penalty.entity.Penalty;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.a404.duckonback.domain.admin.dto.AdminPenaltyListDTO;

@Repository
public interface PenaltyRepository extends JpaRepository<Penalty, Long> {
    List<Penalty> findByUser_Id(Long Id);
    List<Penalty> findByStatus(PenaltyStatus status);
    List<Penalty> findByPenaltyType(PenaltyType type);
    List<Penalty> findByUser_IdAndStatus(Long userId, PenaltyStatus status);

    @Modifying
    @Transactional
    @Query("""
    update Penalty p
    set p.status = com.a404.duckonback.common.enums.PenaltyStatus.EXPIRED
    where p.user.id = :userId
      and p.status = com.a404.duckonback.common.enums.PenaltyStatus.ACTIVE
      and p.endAt < :now
    """)
    void expireOldPenalties(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now);

    @Query("""
      SELECT new com.a404.duckonback.domain.admin.dto.AdminPenaltyListDTO
        (p.penaltyId, p.user.id, u.nickname, p.reason, p.penaltyType, p.status, p.startAt, p.endAt) 
      FROM Penalty p
      JOIN p.user u
      """)
    Page<AdminPenaltyListDTO> findAllBy(Pageable pageable);

}
