package com.a404.duckonback.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatMessageRepository repo;

    public ChatMessage save(ChatMessageDTO dto) {
        ChatMessage msg = ChatMessage.builder()
                .artistId(dto.getArtistId())
                .senderId(dto.getSenderId())
                .senderName(dto.getSenderName())
                .content(dto.getContent())
                .sentAt(LocalDateTime.now())
                .build();
        return repo.save(msg);
    }

    public List<ChatMessage> getHistory(String artistId) {
        return repo.findByArtistIdOrderBySentAtAsc(artistId);
    }
}
