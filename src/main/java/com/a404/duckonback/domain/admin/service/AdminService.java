package com.a404.duckonback.domain.admin.service;

import com.a404.duckonback.common.dto.PageResponse;
import com.a404.duckonback.domain.admin.dto.AdminUserListDTO;
import com.a404.duckonback.domain.admin.dto.AdminArtistListDTO;
import com.a404.duckonback.domain.admin.dto.AdminUserDetailDTO;
import com.a404.duckonback.domain.admin.dto.UserSearchConditionDTO;
import com.a404.duckonback.domain.admin.dto.AdminPenaltyListDTO;
import com.a404.duckonback.domain.admin.dto.AdminPenaltyDetailDTO;

public interface AdminService {
    PageResponse<AdminUserListDTO> getAdminUserList(int page, int size);
    PageResponse<AdminArtistListDTO> getAllArtists(int page, int size);
    AdminUserDetailDTO getUserDetail(String userId);
    PageResponse<AdminUserListDTO> searchAdminUserList(UserSearchConditionDTO condition, int page, int size);
    PageResponse<AdminPenaltyListDTO> getPenaltyList(int page, int size);
    AdminPenaltyDetailDTO getPenaltyDetail(Long penaltyId);
}
