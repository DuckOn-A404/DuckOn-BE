package com.a404.duckonback.chat;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findByArtistIdOrderBySentAtAsc(String artistId);
}
