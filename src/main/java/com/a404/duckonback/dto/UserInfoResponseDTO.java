package com.a404.duckonback.dto;

import com.a404.duckonback.enums.UserRole;
import lombok.*;
import org.springframework.context.support.BeanDefinitionDsl;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponseDTO {
    private String email;
    private String userId;
    private String nickname;
    private String role;
    private String language;
    private String imgUrl;
    private List<Integer> artistList;
    private int followingCount;
    private int followerCount;
    private LocalDateTime bannedTill;
    private String password;
    private List<RoomDTO> roomList;

}
