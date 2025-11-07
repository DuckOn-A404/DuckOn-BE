package com.a404.duckonback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "밈 생성 요청: 한 번에 최대 3개의 밈 업로드")
public class MemeCreateRequestDTO {

    @Schema(description = "업로드할 밈 목록(1~3개)")
    @Valid
    @Size(min = 1, max = 3, message = "밈은 1개 이상 3개 이하로 업로드할 수 있습니다.")
    private List<@Valid MemeItem> memes;

}
