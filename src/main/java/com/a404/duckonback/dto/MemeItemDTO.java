package com.a404.duckonback.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemeItemDTO {
    Long memeId;
    String memeUrl;
}
