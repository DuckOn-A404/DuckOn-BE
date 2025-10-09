package com.a404.duckonback.dto;

import com.a404.duckonback.enums.EmailPurpose;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendEmailCodeRequest {
    @Email @NotNull
    private String email;

    @NotNull
    private EmailPurpose emailPurpose;
}
