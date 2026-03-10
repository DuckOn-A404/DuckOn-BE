package com.a404.duckonback.domain.admin.service;

import com.a404.duckonback.common.dto.PageResponse;
import com.a404.duckonback.common.enums.PenaltyStatus;
import com.a404.duckonback.common.exception.CustomException;
import com.a404.duckonback.domain.admin.dto.AdminUserListDTO;
import com.a404.duckonback.domain.admin.dto.UserSearchConditionDTO;
import com.a404.duckonback.domain.admin.dto.AdminArtistListDTO;
import com.a404.duckonback.domain.admin.dto.AdminUserDetailDTO;
import com.a404.duckonback.domain.artist.artist.entity.Artist;
import com.a404.duckonback.domain.artist.artist.repository.ArtistFollowRepository;
import com.a404.duckonback.domain.artist.artist.repository.ArtistRepository;
import com.a404.duckonback.domain.penalty.entity.Penalty;
import com.a404.duckonback.domain.penalty.repository.PenaltyRepository;
import com.a404.duckonback.domain.report.entity.Report;
import com.a404.duckonback.domain.report.repository.ReportRepository;
import com.a404.duckonback.domain.user.entity.User;
import com.a404.duckonback.domain.user.repository.UserRepository;
import com.a404.duckonback.common.response.ErrorCode;
import com.a404.duckonback.domain.admin.dto.AdminPenaltyListDTO;
import com.a404.duckonback.domain.admin.dto.AdminPenaltyDetailDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;
    private final PenaltyRepository penaltyRepository;
    private final ReportRepository reportRepository;
    private final ArtistFollowRepository artistFollowRepository;

    @Override
    public PageResponse<AdminUserListDTO> getAdminUserList(int page, int size) {
        int safePage = Math.max(page - 1, 0); // 페이지 번호는 0부터 시작
        int safeSize = Math.min(Math.max(size, 1), 100); // 페이지 크기는 1에서 100 사이로 제한

        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<AdminUserListDTO> pageResult = userRepository.getAdminUserList(pageable);

        int totalPages = pageResult.getTotalPages();
        if(safePage >= totalPages && totalPages > 0) {
            throw new CustomException(ErrorCode.EXCEED_TOTAL_PAGES);
        }

        return PageResponse.from1Base(pageResult);
    }

    @Override
    public PageResponse<AdminArtistListDTO> getAllArtists(int page, int size) {
        int safePage = Math.max(page - 1, 0); 
        int safeSize = Math.min(Math.max(size, 1), 100); 

        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<Artist> pageResult = artistRepository.findAll(pageable);

        int totalPages = pageResult.getTotalPages();
        if(safePage >= totalPages && totalPages > 0) {
            throw new CustomException(ErrorCode.EXCEED_TOTAL_PAGES);
        }

        Page<AdminArtistListDTO> dtoPage = pageResult.map(AdminArtistListDTO::fromEntity);
        return PageResponse.from1Base(dtoPage);
    }

    @Override
    public AdminUserDetailDTO getUserDetail(String userId) {
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        // 만료된 제재 처리 후 활성 제재 조회
        penaltyRepository.expireOldPenalties(user.getId(), LocalDateTime.now());
        List<Penalty> activePenalties = penaltyRepository.findByUser_IdAndStatus(
                user.getId(), PenaltyStatus.ACTIVE);

        // 최근 신고당한 내역 (최신순 5건)
        List<Report> recentReports = reportRepository.findTop5ByReported_IdOrderByReportedAtDesc(
                user.getId());

        // 팔로우 중인 아티스트 이름 목록
        List<String> followingArtists = artistFollowRepository.findArtistNamesByUserId(user.getId());

        return AdminUserDetailDTO.fromEntity(
                user, activePenalties, recentReports, followingArtists);
    }

    @Override
    public PageResponse<AdminUserListDTO> searchAdminUserList(UserSearchConditionDTO condition, int page, int size) {
        int safePage = Math.max(page - 1, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<AdminUserListDTO> pageResult = userRepository.searchAdminUserList(
            condition.getKeyword(), 
            condition.getRole(), 
            pageable);
        return PageResponse.from1Base(pageResult);
    }

    @Override
    public PageResponse<AdminPenaltyListDTO> getPenaltyList(int page, int size) {
        int safePage = Math.max(page - 1, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<AdminPenaltyListDTO> pageResult = penaltyRepository.findAllBy(pageable);
        return PageResponse.from1Base(pageResult);
    }

    @Override
    public AdminPenaltyDetailDTO getPenaltyDetail(Long penaltyId) {
        Penalty penalty = penaltyRepository.findById(penaltyId)
                .orElseThrow(() -> new CustomException(ErrorCode.PENALTY_NOT_FOUND));
        User user = userRepository.findById(penalty.getUser().getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return AdminPenaltyDetailDTO.fromEntity(penalty, user);
    }
}
