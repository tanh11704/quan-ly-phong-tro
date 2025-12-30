package com.tpanh.backend.security;

import com.tpanh.backend.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final SecurityErrorResponseWriter errorResponseWriter;

    @Override
    public void handle(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final AccessDeniedException accessDeniedException)
            throws IOException {

        log.debug(
                "Access denied to {}: {}",
                request.getRequestURI(),
                accessDeniedException.getMessage());

        errorResponseWriter.writeErrorResponse(
                response, HttpServletResponse.SC_FORBIDDEN, ErrorCode.FORBIDDEN);
    }
}
