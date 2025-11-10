package com.a404.duckonback.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RandomMemeItemDTO {
    Long memeId;
    String memeUrl;
    List<String> tags;
}
