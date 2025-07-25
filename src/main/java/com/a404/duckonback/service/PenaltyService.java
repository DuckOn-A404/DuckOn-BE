package com.a404.duckonback.service;

import com.a404.duckonback.entity.Penalty;
import com.a404.duckonback.enums.PenaltyStatus;
import com.a404.duckonback.enums.PenaltyType;

import java.util.List;
import java.util.Optional;

public interface PenaltyService {
    Penalty createPenalty(Penalty penalty);
    Optional<Penalty> getPenaltyById(Long penaltyId);
    List<Penalty> getAllPenalties();
    Penalty updatePenalty(Long penaltyId, Penalty updatedPenalty);
    void deletePenalty(Long penaltyId);

    List<Penalty> getPenaltiesByUser(Long id);
    List<Penalty> getPenaltiesByStatus(PenaltyStatus status);
    List<Penalty> getPenaltiesByType(PenaltyType type);
}
