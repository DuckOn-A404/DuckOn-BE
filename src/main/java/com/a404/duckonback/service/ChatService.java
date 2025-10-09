package com.a404.duckonback.service;

import com.a404.duckonback.dto.ChatMessageRequestDTO;
import com.a404.duckonback.dto.ChatMessageResponseDTO;
import com.a404.duckonback.entity.ChatMessage;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.repository.ChatMessageRepository;
import com.a404.duckonback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final SubjectFollowService subjectFollowService;

    public ChatMessage sendMessage(Long userPk, String subjectId, ChatMessageRequestDTO dto) {
        // 1) 팔로우 여부 체크
        if (!subjectFollowService.isFollowingSubject(userPk, Long.valueOf(subjectId))) {
            throw new CustomException(
                "해당 대상을 팔로우한 사용자만 채팅할 수 있습니다.",
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
                .subjectId(subjectId)
                .senderId(userPk)
                .senderUserId(user.getUserId())
                .senderNickname(user.getNickname())
                .content(dto.getContent())
                .sentAt(Instant.now())
                .build();
        return chatMessageRepository.save(msg);
    }

    public List<ChatMessageResponseDTO> getHistory(String subjectId) {
        return chatMessageRepository.findBySubjectIdOrderBySentAtAsc(subjectId).stream()
            .map(ChatMessageResponseDTO::fromEntity)
            .toList();
    }

    public List<ChatMessageResponseDTO> getHistorySince(String subjectId, Instant since) {
        return chatMessageRepository.findBySubjectIdAndSentAtAfterOrderBySentAtAsc(subjectId, since)
            .stream()
            .map(ChatMessageResponseDTO::fromEntity)
            .toList();
    }
}
