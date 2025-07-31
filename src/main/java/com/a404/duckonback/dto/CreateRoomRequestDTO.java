package com.a404.duckonback.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreateRoomRequestDTO {
    private Long artistId;
    private String title;
    private String hostId;
    private MultipartFile thumbnailImg;

    private boolean isLocked;
    private String entryQuestion;
    private String entryAnswer;
}