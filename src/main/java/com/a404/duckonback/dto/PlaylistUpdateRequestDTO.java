// com.a404.duckonback.dto.PlaylistUpdateRequestDTO
package com.a404.duckonback.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PlaylistUpdateRequestDTO {
    @NotNull
    private List<String> playlist; // 전체 플레이리스트(영상 ID 배열)
    private Integer nextIndex;     // 다음 재생할 인덱스 (클라이언트 계산값)
}
