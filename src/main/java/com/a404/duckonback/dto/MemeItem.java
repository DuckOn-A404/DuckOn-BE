package com.a404.duckonback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "단일 밈 업로드 항목")
public class MemeItem {

    @Schema(
            description = "밈 이미지 파일(jpeg/png/webp 등)",
            requiredMode = Schema.RequiredMode.REQUIRED,
            type = "string",
            format = "binary"
    )
    @NotNull(message = "이미지 파일은 필수입니다.")
    private MultipartFile image;

    @Schema(
            description = "태그들(중복 불가). 비워두어도 됨",
            example = "[\"funny\",\"idol\",\"stage\"]"
    )
    @Size(max = 20, message = "태그는 최대 20개까지 허용됩니다.")
    private Set<String> tags;
}
