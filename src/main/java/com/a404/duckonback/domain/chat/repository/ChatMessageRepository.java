package com.a404.duckonback.domain.chat.repository;

import com.a404.duckonback.domain.chat.entity.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findByArtistIdOrderBySentAtAsc(String artistId);
    List<ChatMessage> findByArtistIdAndSentAtAfterOrderBySentAtAsc(String artistId, Instant since);
}

