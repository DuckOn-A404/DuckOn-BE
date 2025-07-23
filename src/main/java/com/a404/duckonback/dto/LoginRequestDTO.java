package com.a404.duckonback.dto;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String email;
    private String userId;
    private String password;

    public String getLoginIdentifier(){
        return (email != null && !email.isBlank()) ? email : userId;
    }
}
