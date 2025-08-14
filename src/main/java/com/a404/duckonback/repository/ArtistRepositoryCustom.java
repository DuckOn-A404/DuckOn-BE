package com.a404.duckonback.repository;

import com.a404.duckonback.dto.ArtistDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ArtistRepositoryCustom {
    /**
     * keyword(옵션), sort(followers|name|debut), order(asc|desc)를 적용하여
     * N+1 없이 페이지 단위로 ArtistDTO를 반환합니다.
     */
    Page<ArtistDTO> pageArtists(Pageable pageable, String sort, String order, String keyword);
}