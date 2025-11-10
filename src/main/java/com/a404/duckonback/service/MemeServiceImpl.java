package com.a404.duckonback.service;

import com.a404.duckonback.dto.MemeCreateRequestDTO;
import com.a404.duckonback.dto.MemeCreateResponseDTO;
import com.a404.duckonback.entity.Meme;
import com.a404.duckonback.entity.MemeTag;
import com.a404.duckonback.entity.Tag;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.dto.MemeCreateResponseDTO.MemeInfoDTO;
import com.a404.duckonback.repository.MemeRepository;
import com.a404.duckonback.repository.MemeTagRepository;
import com.a404.duckonback.repository.TagRepository;
import com.a404.duckonback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MemeServiceImpl implements MemeService {

    private final MemeS3Service memeS3Service;
    private final MemeRepository memeRepository;
    private final TagRepository tagRepository;
    private final MemeTagRepository memeTagRepository;
    private final UserRepository userRepository;

    @Override
    public MemeCreateResponseDTO createMeme(Long userId, MemeCreateRequestDTO req) {
        User creator = userRepository.getReferenceById(userId);

        List<MemeInfoDTO> resultList = new ArrayList<>();

        handleOne(creator, req.getImage1(), req.getTags1()).ifPresent(resultList::add);
        handleOne(creator, req.getImage2(), req.getTags2()).ifPresent(resultList::add);
        handleOne(creator, req.getImage3(), req.getTags3()).ifPresent(resultList::add);

        if (resultList.isEmpty()) {
            throw new IllegalArgumentException("업로드할 밈 이미지가 최소 1개 이상 필요합니다.");
        }

        return MemeCreateResponseDTO.builder()
                .memes(resultList)
                .build();
    }

    private Optional<MemeInfoDTO> handleOne(User creator,
                                            MultipartFile file,
                                            Set<String> rawTags) {
        if (file == null || file.isEmpty()) {
            return Optional.empty();
        }

        // 1) S3 업로드 → CDN URL
        var upload = memeS3Service.uploadMeme(file);

        // 2) Meme 엔티티 저장
        Meme meme = Meme.builder()
                .creator(creator)
                .imageUrl(upload.getCdnUrl())
                .usageCnt(0)
                .downloadCnt(0)
                .createdAt(LocalDateTime.now())
                .build();

        meme = memeRepository.save(meme);

        // 3) 태그 처리 (null-safe + trim + 중복 제거)
        Set<String> normalizedTags = Optional.ofNullable(rawTags)
                .orElse(Collections.emptySet())
                .stream()
                .map(t -> t == null ? "" : t.trim())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new)); // 순서 유지

        for (String tagName : normalizedTags) {
            Tag tag = tagRepository.findByTagName(tagName)
                    .orElseGet(() -> tagRepository.save(
                            Tag.builder().tagName(tagName).build()
                    ));

            MemeTag mt = MemeTag.of(meme, tag);
            memeTagRepository.save(mt);
        }

        log.info("✅ Meme created: id={}, url={}", meme.getId(), meme.getImageUrl());

        MemeInfoDTO dto = MemeInfoDTO.builder()
                .memeId(meme.getId())
                .imageUrl(meme.getImageUrl())
                .tags(normalizedTags)
                .build();

        return Optional.of(dto);
    }
}
