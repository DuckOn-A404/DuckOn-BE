package com.a404.duckonback.service;

import com.a404.duckonback.dto.ImageDocument;
import com.a404.duckonback.dto.SearchResponseDto;

import java.io.IOException;
import java.util.Map;

public interface SearchService {

    SearchResponseDto searchByTag(String queryTerm, String mode) throws IOException;

    void indexImage(ImageDocument imageDocument) throws IOException;

    // 테스트 메서드들
    Map<String, Object> testConnection() throws IOException;
    
    Map<String, Object> getAllDocuments() throws IOException;

}
