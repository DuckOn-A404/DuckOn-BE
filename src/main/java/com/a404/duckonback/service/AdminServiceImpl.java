package com.a404.duckonback.service;

import com.a404.duckonback.dto.AdminUserListDTO;
import com.a404.duckonback.dto.common.PageResponse;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.repository.UserRepository;
import com.a404.duckonback.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;

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
}
