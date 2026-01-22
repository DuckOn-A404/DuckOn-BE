package com.a404.duckonback.service;

import com.a404.duckonback.dto.AdminUserListDTO;
import com.a404.duckonback.dto.common.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {
    PageResponse<AdminUserListDTO> getAdminUserList(int page, int size);
}
