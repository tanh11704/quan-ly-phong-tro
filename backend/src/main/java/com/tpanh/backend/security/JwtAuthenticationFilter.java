package com.tpanh.backend.security;

import com.tpanh.backend.config.SecurityConstants;
import com.tpanh.backend.dto.JwtPayload;
import com.tpanh.backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {
        final String path = request.getServletPath();
        return Arrays.stream(SecurityConstants.AUTHENTICATION_BYPASS_PATHS)
                .anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain)
            throws ServletException, IOException {
        try {
            final var jwt = getJwtFromRequest(request);
            if (StringUtils.hasText(jwt)) {
                jwtService
                        .parseAndValidate(jwt)
                        .ifPresent(payload -> setAuthentication(payload, request));
            }
        } catch (final Exception e) {
            SecurityContextHolder.clearContext();
            log.debug("JWT authentication failed: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    private void setAuthentication(final JwtPayload payload, final HttpServletRequest request) {
        final var authorities = payload.roles().stream().map(SimpleGrantedAuthority::new).toList();
        final var principal = new UserPrincipal(payload.userId(), payload.roles());
        final var authentication =
                new UsernamePasswordAuthenticationToken(principal, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }

    private String getJwtFromRequest(final HttpServletRequest request) {
        final var bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
