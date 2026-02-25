package com.a404.duckonback.domain.notification.dto.payload;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EmptyPayload implements NotificationPayload{
    public static final EmptyPayload INSTANCE = new EmptyPayload();
}
