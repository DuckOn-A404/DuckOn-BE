package com.a404.duckonback.service;

import com.a404.duckonback.config.ServiceProperties;
import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailSenderImpl implements EmailSender {

    private final JavaMailSender mailSender;
    private final ServiceProperties props;

    @Override
    public void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            msg.setFrom(new InternetAddress(props.getMailFrom(), props.getMailBrand()));
            msg.setRecipients(Message.RecipientType.TO, to);
            msg.setSubject(subject, "UTF-8");
            msg.setContent(htmlBody, "text/html; charset=UTF-8");
            mailSender.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("메일 전송 실패", e);
        }
    }
}
