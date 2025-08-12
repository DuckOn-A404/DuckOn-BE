package com.a404.duckonback.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailInfoResponseDTO {
    private String email;
    private String userId;
    private String nickname;
    private String role;
    private String language;
    private String imgUrl;
    private List<Long> artistList;
    private int followingCount;
    private int followerCount;
    private String password;
    private boolean socialLogin;
    private List<RoomDTO> roomList;
    private List<PenaltyDTO> penaltyList;

}
