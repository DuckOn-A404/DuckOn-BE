package com.a404.duckonback.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomEnterRequestDTO {
    private String answer; // 사용자가 입력한 입장 정답
}
