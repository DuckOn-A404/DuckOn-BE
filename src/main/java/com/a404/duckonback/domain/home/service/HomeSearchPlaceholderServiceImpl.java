package com.a404.duckonback.domain.home.service;

import com.a404.duckonback.common.exception.CustomException;
import com.a404.duckonback.common.response.ErrorCode;
import com.a404.duckonback.domain.home.dto.HomeSearchPlaceholderResponseDTO;
import com.a404.duckonback.domain.home.entity.HomeSearchPlaceholder;
import com.a404.duckonback.domain.home.repository.HomeSearchPlaceholderRepository;
import com.a404.duckonback.domain.user.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HomeSearchPlaceholderServiceImpl implements HomeSearchPlaceholderService {
    private static final Long SINGLE_ROW_ID = 1L;
    private static final String REDIS_KEY = "duckon:home:placeholders";
    private static final Duration REDIS_TTL = Duration.ofHours(6);

    private final HomeSearchPlaceholderRepository homeSearchPlaceholderRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public HomeSearchPlaceholderResponseDTO getPlaceholders(){
        // 1. Redis hit면 그대로 반환
        String cached = stringRedisTemplate.opsForValue().get(REDIS_KEY);
        if(cached != null && !cached.isBlank()){
            try{
                return objectMapper.readValue(cached, HomeSearchPlaceholderResponseDTO.class);
            } catch (Exception e){
                log.error("Failed to parse cached placeholders, will fetch from DB. cache={}", cached, e);
            }
        }

        // 2. DB 조 (없으면 기본값 생성)
        HomeSearchPlaceholder entity = homeSearchPlaceholderRepository.findById(SINGLE_ROW_ID)
                .orElseGet(() -> homeSearchPlaceholderRepository.save(
                        HomeSearchPlaceholder.initDefault(
                                null,
                                toJson(List.of("제니 MMA", "지디 Home Sweet Home", "알파드라이브원 4k"))
                        )
                ));

        List<String> items = fromJson(entity.getItemsJson());

        HomeSearchPlaceholderResponseDTO dto = HomeSearchPlaceholderResponseDTO.builder()
                .items(items)
                .version(entity.getVersion())
                .updatedAt(entity.getUpdatedAt())
                .build();


        // 3. Redis 캐싱 (실패해도 서비스는 정상적으로 반환)
        try{
            stringRedisTemplate.opsForValue().set(REDIS_KEY, objectMapper.writeValueAsString(dto), REDIS_TTL);
        } catch (Exception e){
            log.error("Failed to cache placeholders in Redis, but will return the response. dto={}", dto, e);
        }
        return dto;
    }

    @Override
    @Transactional
    public HomeSearchPlaceholderResponseDTO updatePlaceholders(List<String> items, Long adminUserId){
        // 1. trim, 빈 값 제거, 중복 제거(순서 유지)
        List<String> normalized = items.stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();

        if(normalized.isEmpty()){
            throw new CustomException(ErrorCode.INVALID_PLACEHOLDERS);
        }

        String json = toJson(normalized);

        HomeSearchPlaceholder entity = homeSearchPlaceholderRepository.findById(SINGLE_ROW_ID)
                .orElse(null);

        // 2. 있으면 업데이트, 없으면 생성
        if (entity == null) {
            entity = HomeSearchPlaceholder.initDefault(adminUserId, json);
        } else {
            entity.updateItems(json, adminUserId);
        }

        entity = homeSearchPlaceholderRepository.save(entity);

        // 3. 캐시 무효화
        try{
            stringRedisTemplate.delete(REDIS_KEY);
        } catch (Exception e){
            log.warn("Failed to delete redis cache. key={}", REDIS_KEY, e);
        }

        return HomeSearchPlaceholderResponseDTO.builder()
                .items(normalized)
                .version(entity.getVersion())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private List<String> fromJson(String itemsJson){
        try{
            return objectMapper.readValue(itemsJson, new TypeReference<List<String>>() {});
        } catch (Exception e){
            // DB 값이 깨졌을 때의 대응. 로그는 남기지만, 기본값으로 복구해서 서비스는 유지한다.
            log.error("placeholders json 파싱 실패, 기본값으로 복구. json={}", itemsJson, e);
            return List.of();
        }
    }

    private String toJson(List<String> items){
        try{
            return objectMapper.writeValueAsString(items);
        } catch (Exception e){
            throw new IllegalStateException("placeholders json 변환 실패", e);
        }
    }
}
