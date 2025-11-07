package com.a404.duckonback.service;

import com.a404.duckonback.dto.MemeCreateRequestDTO;
import com.a404.duckonback.dto.MemeItem;
import com.a404.duckonback.entity.Meme;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class MemeServiceImpl {
    public void createMeme(Long userId, MemeCreateRequestDTO request){
        for(MemeItem memeItem : request.getMemes()){
            MultipartFile image = memeItem.getImage();

            Set<String> tags = memeItem.getTags();
        }
        return null;
    }
}
