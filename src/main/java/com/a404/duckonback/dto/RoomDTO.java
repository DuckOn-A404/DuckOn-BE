package com.a404.duckonback.dto;

import com.a404.duckonback.entity.Room;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDTO {

    private Long roomId;
    private String title;
    private String imgUrl;
    private LocalDateTime createdAt;

    /** 방 만든 유저의 userId(표시/링크용). PK가 아니라는 점 주의 */
    private String creatorId;

    /** 주체(아티스트/선수/크리에이터 등) 기본키 */
    private Long subjectId;

    /** FE 라우팅용 불변 슬러그 (/subject/:slug) */
    private String subjectSlug;

    /** 화면 표시명(요청 로케일 우선, 없으면 native 등 정책에 따라 산출) */
    private String subjectDisplayName;

    /**
     * 헬퍼: Room + 미리 계산된 displayName으로 DTO 생성
     * displayName은 Repository/Resolver에서 계산해서 넘겨주는 것을 권장
     */
    public static RoomDTO fromEntity(Room room, String displayName) {
        return RoomDTO.builder()
            .roomId(room.getRoomId())
            .title(room.getTitle())
            .imgUrl(room.getImgUrl())
            .createdAt(room.getCreatedAt())
            .creatorId(room.getCreator() != null ? room.getCreator().getUserId() : null)
            .subjectId(room.getSubject() != null ? room.getSubject().getId() : null)
            .subjectSlug(room.getSubject() != null ? room.getSubject().getSlug() : null)
            .subjectDisplayName(displayName)
            .build();
    }
}
