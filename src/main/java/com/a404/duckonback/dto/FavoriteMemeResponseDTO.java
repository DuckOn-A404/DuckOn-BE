package com.a404.duckonback.dto;


import lombok.*;

import java.util.List;

@Getter
@Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FavoriteMemeResponseDTO {
    private int page;
    private int size;
    private int total;              // 전체 개수
    private List<FavoriteMemeItemDTO> items;
}