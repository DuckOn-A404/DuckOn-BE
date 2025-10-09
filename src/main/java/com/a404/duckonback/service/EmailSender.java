package com.a404.duckonback.service;

public interface EmailSender {
    void send(String to, String subject, String htmlBody);
}
