package com.a404.duckonback.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MemeCreateItem {
    private Long memeId;
    private String imageUrl;
    private List<String> tags;
}