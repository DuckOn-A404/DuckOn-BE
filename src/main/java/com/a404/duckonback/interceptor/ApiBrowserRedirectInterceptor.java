package com.a404.duckonback.interceptor;

//import com.a404.duckonback.service.ArtistService;
import com.a404.duckonback.service.SubjectService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ApiBrowserRedirectInterceptor implements HandlerInterceptor {

    private final SubjectService subjectService; // id -> slug 조회용

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws IOException {
        String uri = req.getRequestURI();          // /api/chat/artist/1/message
        String accept = req.getHeader("Accept");   // text/html 이면 브라우저 네비게이션일 확률 높음
        String auth   = req.getHeader("Authorization");

        if (req.getMethod().equals("GET")
            && uri.matches("^/api/chat/subject/\\d+/message$")
            && (accept != null && accept.contains("text/html"))
            && (auth == null || auth.isBlank())) {

            String subjectId = uri.replaceAll("^/api/chat/subject/(\\d+)/message$", "$1");
            String slug = subjectService.findSlugById(Long.parseLong(subjectId));
            res.setStatus(HttpServletResponse.SC_FOUND); // 302
            res.setHeader("Location", "/subject/" + slug);
            return false; // 컨트롤러로 넘기지 않음
        }
        return true;
    }
}
