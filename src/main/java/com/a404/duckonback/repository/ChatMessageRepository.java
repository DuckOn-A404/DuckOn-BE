package com.a404.duckonback.repository;

import com.a404.duckonback.entity.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findByArtistIdOrderBySentAtAsc(String artistId);
    List<ChatMessage> findByArtistIdAndSentAtAfterOrderBySentAtAsc(String artistId, LocalDateTime since);
}

