package com.a404.duckonback.service;

import com.a404.duckonback.dto.ChatMessageRequestDTO;
import com.a404.duckonback.dto.ChatMessageResponseDTO;
import com.a404.duckonback.dto.UserRankDTO;
import com.a404.duckonback.entity.ChatMessage;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.entity.UserEngagementStat;
import com.a404.duckonback.enums.RankLevel;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.repository.ChatMessageRepository;
import com.a404.duckonback.repository.UserEngagementStatRepository;
import com.a404.duckonback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ArtistFollowService artistFollowService;
    private final UserRankService userRankService;
    private final UserService userService;
    private final UserEngagementStatRepository userEngagementStatRepository;

    public ChatMessage sendMessage(Long userPk, String artistId, ChatMessageRequestDTO dto) {
        // 1) 팔로우 여부 체크
        if (!artistFollowService.isFollowingArtist(userPk, Long.valueOf(artistId))) {
            throw new CustomException(
                    "해당 아티스트를 팔로우한 사용자만 채팅할 수 있습니다.",
                    HttpStatus.FORBIDDEN
            );
        }
        // 2) 사용자 정보 조회
        User user = userRepository.findByIdAndDeletedFalse(userPk);
        if (user == null) {
            throw new CustomException("존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND);
        }
        // 3) 엔티티로 변환 후 저장
        ChatMessage msg = ChatMessage.builder()
                .artistId(artistId)
                .senderId(userPk)
                .senderUserId(user.getUserId())
                .senderNickname(user.getNickname())
                .content(dto.getContent())
                .sentAt(Instant.now())
                .build();
        return chatMessageRepository.save(msg);
    }

    public List<ChatMessageResponseDTO> getHistory(String artistId) {
        return chatMessageRepository.findByArtistIdOrderBySentAtAsc(artistId).stream()
                .map(ChatMessageResponseDTO::fromEntity)
                .toList();

//        List<ChatMessage> chats =
//                chatMessageRepository.findByArtistIdOrderBySentAtAsc(artistId);
//        Set<Long> senderIds = chats.stream()
//                .map(ChatMessage::getSenderId)
//                .filter(Objects::nonNull)
//                .collect(Collectors.toSet());
//
//        Map<Long, UserEngagementStat> statsByUserId = userEngagementStatRepository
//                .findAllById(senderIds) // 또는 findByIdIn(senderIds)
//                .stream()
//                .collect(Collectors.toMap(UserEngagementStat::getId, Function.identity()));
//
//        return chats.stream()
//                .map(chat -> {
//                    ChatMessageResponseDTO dto = ChatMessageResponseDTO.fromEntity(chat);
//
//                    UserEngagementStat stat = statsByUserId.get(chat.getSenderId());
//                    // gradeComposite → RankLevel, roomCreateCount 세팅
//                    UserRankDTO rankDTO = UserRankDTO.builder()
//                            .roomCreateCount(stat != null
//                                    ? stat.getRoomCreateCount().longValue()
//                                    : 0L)
//                            .rankLevel(RankLevel.fromString(
//                                    stat != null ? stat.getGradeComposite() : null))
//                            .build();
//
//                    dto.setUserRank(rankDTO);
//                    return dto;
//                })
//                .toList();

    }

    public List<ChatMessageResponseDTO> getHistorySince(String artistId, Instant since) {
        return chatMessageRepository.findByArtistIdAndSentAtAfterOrderBySentAtAsc(artistId, since)
                .stream()
                .map(ChatMessageResponseDTO::fromEntity)
                .toList();
//        // 1) 채팅 일괄 조회 (Mongo 1회)
//        List<ChatMessage> chats = chatMessageRepository
//                .findByArtistIdAndSentAtAfterOrderBySentAtAsc(artistId, since);
//
//        // 2) 고유 senderId 집합 추출
//        Set<Long> senderIds = chats.stream()
//                .map(ChatMessage::getSenderId)
//                .filter(Objects::nonNull)
//                .collect(Collectors.toSet());
//
//        // 3) 통계(랭크 소스) 일괄 조회 (RDB 1회)
//        Map<Long, UserEngagementStat> statsByUserId = userEngagementStatRepository
//                .findAllById(senderIds) // 또는 findByIdIn(senderIds)
//                .stream()
//                .collect(Collectors.toMap(UserEngagementStat::getId, Function.identity()));
//
//        // 4) DTO 매핑 + 랭크 주입
//        return chats.stream()
//                .map(chat -> {
//                    ChatMessageResponseDTO dto = ChatMessageResponseDTO.fromEntity(chat);
//
//                    UserEngagementStat stat = statsByUserId.get(chat.getSenderId());
//                    UserRankDTO rankDTO = UserRankDTO.builder()
//                            .roomCreateCount(stat != null
//                                    ? stat.getRoomCreateCount().longValue()
//                                    : 0L)
//                            .rankLevel(RankLevel.fromString(
//                                    stat != null ? stat.getGradeComposite() : null))
//                            .build();
//
//                    dto.setUserRank(rankDTO);
//                    return dto;
//                })
//                .toList();
    }
}
