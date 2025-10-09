package com.a404.duckonback.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SendEmailCodeResponse {
    private boolean sent;
    private String message;
}
