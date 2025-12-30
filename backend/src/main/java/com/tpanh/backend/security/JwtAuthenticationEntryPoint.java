package com.tpanh.backend.security;

import com.tpanh.backend.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Custom AuthenticationEntryPoint that returns a JSON response for unauthenticated requests.
 * Returns HTTP 401 Unauthorized with a consistent JSON error format.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final SecurityErrorResponseWriter errorResponseWriter;

    @Override
    public void commence(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final AuthenticationException authException)
            throws IOException {

        log.debug(
                "Unauthorized request to {}: {}",
                request.getRequestURI(),
                authException.getMessage());

        errorResponseWriter.writeErrorResponse(
                response, HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
    }
}
