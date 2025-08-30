package com.a404.duckonback.repository;

import com.a404.duckonback.entity.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findBySubjectIdOrderBySentAtAsc(String subjectId);
    List<ChatMessage> findBySubjectIdAndSentAtAfterOrderBySentAtAsc(String subjectId, Instant since);
}
