package com.a404.duckonback.domain.upload.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UploadPurpose {
    ARTIST_CHANGE("uploads/artist-change"),
    ARTIST_IMAGE_TEMP("uploads/temp/artist"),
    RISING_ARTIST_IMAGE_TEMP("uploads/temp/rising-artist"),
    REPORT("uploads/report"),
    FEEDBACK("uploads/feedback");

    private final String basePrefix;
}
