package com.a404.duckonback.common.notification.email;

public interface EmailSender {
    void send(String to, String subject, String htmlBody);
}
