package com.a404.duckonback.domain.admin.service;

import com.a404.duckonback.common.dto.PageResponse;
import com.a404.duckonback.common.exception.CustomException;
import com.a404.duckonback.domain.admin.dto.AdminUserListDTO;
import com.a404.duckonback.domain.admin.dto.AdminArtistListDTO;
import com.a404.duckonback.domain.admin.dto.AdminUserDetailDTO;
import com.a404.duckonback.domain.artist.artist.entity.Artist;
import com.a404.duckonback.domain.artist.artist.repository.ArtistRepository;
import com.a404.duckonback.domain.user.entity.User;
import com.a404.duckonback.domain.user.repository.UserRepository;
import com.a404.duckonback.common.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;

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
        return AdminUserDetailDTO.fromEntity(user);
    }
}
