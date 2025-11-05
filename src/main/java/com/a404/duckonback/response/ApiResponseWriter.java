package com.a404.duckonback.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ApiResponseWriter {
    private final ObjectMapper mapper;

    public void write(HttpServletResponse response, ApiResponseDTO<?> body) throws IOException {
        response.setStatus(body.httpStatus().value()); // 실제 HTTP status
        response.setContentType("application/json;charset=UTF-8");
        mapper.writeValue(response.getOutputStream(), body); // JSON body(status/message/data)
    }
}
