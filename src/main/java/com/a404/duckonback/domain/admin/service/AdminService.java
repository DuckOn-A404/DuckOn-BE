package com.a404.duckonback.domain.admin.service;

import com.a404.duckonback.common.dto.PageResponse;
import com.a404.duckonback.domain.admin.dto.AdminUserListDTO;

public interface AdminService {
    PageResponse<AdminUserListDTO> getAdminUserList(int page, int size);
}
