package com.tpanh.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpanh.backend.dto.ApiResponse;
import com.tpanh.backend.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class SecurityErrorResponseWriter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void writeErrorResponse(
            final HttpServletResponse response, final int httpStatus, final ErrorCode errorCode)
            throws IOException {

        response.setStatus(httpStatus);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        final var apiResponse =
                ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build();

        objectMapper.writeValue(response.getWriter(), apiResponse);
    }
}
