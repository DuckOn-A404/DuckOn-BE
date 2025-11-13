package com.a404.duckonback.service;

import com.a404.duckonback.dto.ImageDocument;
import com.a404.duckonback.dto.SearchResponseDto;

import java.io.IOException;

public interface SearchService {

    SearchResponseDto searchByTag(String queryTerm, String mode) throws IOException;

    void indexImage(ImageDocument imageDocument) throws IOException;

}
