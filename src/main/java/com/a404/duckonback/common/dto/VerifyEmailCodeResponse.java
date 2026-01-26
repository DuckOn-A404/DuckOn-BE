package com.a404.duckonback.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerifyEmailCodeResponse {
    private boolean verified;
    private String message;
}
