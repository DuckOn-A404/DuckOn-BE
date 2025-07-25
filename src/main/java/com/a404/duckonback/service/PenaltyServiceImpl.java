package com.a404.duckonback.service;

import com.a404.duckonback.entity.Penalty;
import com.a404.duckonback.enums.PenaltyStatus;
import com.a404.duckonback.enums.PenaltyType;
import com.a404.duckonback.repository.PenaltyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PenaltyServiceImpl implements PenaltyService {

    private final PenaltyRepository penaltyRepository;

    @Override
    public Penalty createPenalty(Penalty penalty) {
        return penaltyRepository.save(penalty);
    }

    @Override
    public Optional<Penalty> getPenaltyById(Long penaltyId) {
        return penaltyRepository.findById(penaltyId);
    }

    @Override
    public List<Penalty> getAllPenalties() {
        return penaltyRepository.findAll();
    }

    @Override
    public Penalty updatePenalty(Long penaltyId, Penalty updatedPenalty) {
        return penaltyRepository.findById(penaltyId)
                .map(penalty -> {
                    penalty.setUser(updatedPenalty.getUser());
                    penalty.setStartAt(updatedPenalty.getStartAt());
                    penalty.setEndAt(updatedPenalty.getEndAt());
                    penalty.setReason(updatedPenalty.getReason());
                    penalty.setPenaltyType(updatedPenalty.getPenaltyType());
                    penalty.setStatus(updatedPenalty.getStatus());
                    return penaltyRepository.save(penalty);
                })
                .orElseThrow(() -> new IllegalArgumentException("Penalty not found with ID: " + penaltyId));
    }

    @Override
    public void deletePenalty(Long penaltyId) {
        penaltyRepository.deleteById(penaltyId);
    }

    @Override
    public List<Penalty> getPenaltiesByUser(Long id) {
        return penaltyRepository.findByUser_Id(id);
    }

    @Override
    public List<Penalty> getPenaltiesByStatus(PenaltyStatus status) {
        return penaltyRepository.findByStatus(status);
    }

    @Override
    public List<Penalty> getPenaltiesByType(PenaltyType type) {
        return penaltyRepository.findByPenaltyType(type);
    }

    @Override
    public List<Penalty> getActivePenaltiesByUser(Long userId) {
        List<Penalty> penalties = penaltyRepository.findByUser_Id(userId);

        if (penalties == null || penalties.isEmpty()) {
            return Collections.emptyList();
        }

        // Filter active penalties
        return penalties.stream()
                .filter(penalty -> penalty.getStatus() == PenaltyStatus.ACTIVE)
                .toList();
    }
}
