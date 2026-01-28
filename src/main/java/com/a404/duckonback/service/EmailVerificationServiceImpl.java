package com.a404.duckonback.service;

import com.a404.duckonback.config.ServiceProperties;
import com.a404.duckonback.enums.EmailPurpose;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.util.RandomCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final StringRedisTemplate stringRedisTemplate;
    private final EmailSender emailSender;
    private final EmailTemplateRenderer emailTemplateRenderer;
    private final ServiceProperties serviceProperties;

    private String codeKey(String email, EmailPurpose purpose) {
        return "email:code:%s:%s".formatted(purpose.name(), email.toLowerCase());
    }
    private String sendCntKey(String email) {
        return "email:send:cnt:%s".formatted(email.toLowerCase());
    }
    private String verifyCntKey(String email) {
        return "email:verify:cnt:%s".formatted(email.toLowerCase());
    }

    @Override
    public void sendCode(String email, EmailPurpose purpose) {
        // 1) 레이트 리밋(10분 N회)
        String sendKey = sendCntKey(email);
        Long sent = stringRedisTemplate.opsForValue().increment(sendKey);
        if(sent != null && sent == 1L) {
            stringRedisTemplate.expire(sendKey, Duration.ofMinutes(10));
        }
        if(sent == null && sent > serviceProperties.getSendPer10m()){
            throw new CustomException("이메일 발송량이 너무 많습니다. 잠시 후 다시 시도해주세요.", HttpStatus.TOO_MANY_REQUESTS);
        }

        // 2) 인증코드 생성 및 저장
        String code = RandomCodeGenerator.numeric(serviceProperties.getEmailCodeLength());
        String key = codeKey(email, purpose);
        stringRedisTemplate.opsForValue().set(key, code, Duration.ofMinutes(serviceProperties.getEmailCodeTtlSeconds()));

        // 3) 이메일 발송
        String subject = "[%s] 이메일 인증번호입니다.".formatted(serviceProperties.getMailBrand());
        String html = emailTemplateRenderer.renderVerification(serviceProperties.getMailBrand(), code);
        emailSender.send(email, subject, html);
    }

    @Override
    public boolean verifyCode(String email, EmailPurpose purpose, String code) {
        // 0) 시도 횟수 제한(1시간 N회)
        String vKey = verifyCntKey(email);
        Long tries = stringRedisTemplate.opsForValue().increment(vKey);
        if (tries != null && tries == 1L) {
            stringRedisTemplate.expire(vKey, Duration.ofHours(1));
        }
        if (tries != null && tries > serviceProperties.getVerifyPer1h()) {
            throw new CustomException("인증 시도 횟수가 초과되었습니다. 잠시 후 다시 시도하세요.", HttpStatus.TOO_MANY_REQUESTS);
        }

        // 1) 일치/유효기간 확인
        String key = codeKey(email, purpose);
        String saved = stringRedisTemplate.opsForValue().get(key);
        if (saved == null) {
            throw new CustomException("인증번호가 만료되었거나 존재하지 않습니다.", HttpStatus.BAD_REQUEST);
        }
        boolean ok = saved.equals(code);

        // 2) 성공 시 소비(옵션)
        if (ok && serviceProperties.isConsumeOnSuccess()) {
            stringRedisTemplate.delete(key);
        }
        return ok;
    }

}
