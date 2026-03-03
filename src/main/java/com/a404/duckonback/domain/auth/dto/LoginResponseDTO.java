package com.a404.duckonback.domain.auth.dto;

import com.a404.duckonback.domain.user.dto.UserDTO;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponseDTO {

    private String accessToken;
    private UserDTO user;


}
