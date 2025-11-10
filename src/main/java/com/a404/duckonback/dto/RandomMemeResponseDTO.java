package com.a404.duckonback.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RandomMemeResponseDTO {
    int page;
    int size;
    int total;
    List<RandomMemeItemDTO> items;
}
