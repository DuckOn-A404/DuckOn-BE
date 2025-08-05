package com.a404.duckonback.chat;

import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.service.ArtistFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final ArtistFollowService artistFollowService;

    public ChatMessage sendMessage(Long userId, String artistId, ChatMessageDTO dto) {
        // 1) 팔로우 여부 체크
        if (!artistFollowService.isFollowingArtist(userId, Long.valueOf(artistId))) {
            throw new CustomException(
                    "해당 아티스트를 팔로우한 사용자만 채팅할 수 있습니다.",
                    HttpStatus.FORBIDDEN
            );
        }

        // 2) dto → 엔티티 변환 및 저장
        ChatMessage msg = ChatMessage.builder()
                .artistId(artistId)
                .senderId(userId)
                .senderName(dto.getSenderName())
                .content(dto.getContent())
                .sentAt(LocalDateTime.now())
                .build();

        return chatMessageRepository.save(msg);
    }

    public List<ChatMessage> getHistory(String artistId) {
        return chatMessageRepository.findByArtistIdOrderBySentAtAsc(artistId);
    }

    // 이후 메시지만 가져오고 싶다면
    public List<ChatMessage> getHistorySince(String artistId, LocalDateTime since) {
        return chatMessageRepository.findByArtistIdAndSentAtAfterOrderBySentAtAsc(artistId, since);
    }
}
