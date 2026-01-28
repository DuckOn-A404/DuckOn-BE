package com.a404.duckonback.domain.penalty.service;

import com.a404.duckonback.common.enums.PenaltyStatus;
import com.a404.duckonback.common.enums.PenaltyType;
import com.a404.duckonback.domain.penalty.entity.Penalty;

import java.util.List;
import java.util.Optional;

public interface PenaltyService {
    Penalty createPenalty(Penalty penalty);
    Optional<Penalty> getPenaltyById(Long penaltyId);
    List<Penalty> getAllPenalties();
    Penalty updatePenalty(Long penaltyId, Penalty updatedPenalty);
    void deletePenalty(Long penaltyId);
    Boolean isAccountSuspended(Long userId);

    List<Penalty> getPenaltiesByUser(Long id);
    List<Penalty> getPenaltiesByStatus(PenaltyStatus status);
    List<Penalty> getPenaltiesByType(PenaltyType type);
    List<Penalty> getActivePenaltiesByUser(Long userId);
}
