package com.a404.duckonback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "밈 생성 요청: 한 번에 최대 3개의 밈 업로드")
public class MemeCreateRequestDTO {

    @Schema(type = "string", format = "binary", description = "1번 밈 이미지 파일")
    private MultipartFile image1;
    @Schema(description = "1번 밈 태그들", example = "[\"funny\",\"idol\"]")
    @Size(max = 20, message = "태그는 최대 20개까지 허용됩니다.")
    private Set<String> tags1;

    @Schema(type = "string", format = "binary", description = "2번 밈 이미지 파일(선택)")
    private MultipartFile image2;
    @Schema(description = "2번 밈 태그들")
    @Size(max = 20, message = "태그는 최대 20개까지 허용됩니다.")
    private Set<String> tags2;

    @Schema(type = "string", format = "binary", description = "3번 밈 이미지 파일(선택)")
    private MultipartFile image3;
    @Schema(description = "3번 밈 태그들")
    @Size(max = 20, message = "태그는 최대 20개까지 허용됩니다.")
    private Set<String> tags3;
}
