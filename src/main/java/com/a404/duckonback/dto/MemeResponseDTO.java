package com.a404.duckonback.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemeResponseDTO {
    int page;
    int size;
    int total;
    List<MemeItemDTO> items;
}
