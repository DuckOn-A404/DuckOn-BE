package com.a404.duckonback.domain.admin.dto;

import com.a404.duckonback.common.enums.UserRole;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserSearchConditionDTO {
    private String keyword;
    private UserRole role;
}
