package com.tpanh.backend.service;

import com.tpanh.backend.config.SentryProperties;
import com.tpanh.backend.dto.SentryEventDTO;
import com.tpanh.backend.dto.SentryIssueDTO;
import com.tpanh.backend.dto.SentryIssueListResponseDTO;
import com.tpanh.backend.exception.AppException;
import com.tpanh.backend.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class SentryService {
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ISSUES_ENDPOINT = "/projects/{organization}/{project}/issues/";
    private static final String ISSUE_DETAIL_ENDPOINT =
            "/projects/{organization}/{project}/issues/{issueId}/";
    private static final String EVENTS_ENDPOINT = "/projects/{organization}/{project}/events/";

    private final RestTemplate restTemplate;
    private final SentryProperties sentryProperties;

    public SentryIssueListResponseDTO getIssues(
            final Integer page,
            final Integer pageSize,
            final String status,
            final String level,
            final String query) {
        try {
            final var issues = fetchIssuesFromSentry(status, level, query);
            final var paginatedIssues = applyPagination(issues, page, pageSize);
            return buildIssueListResponse(paginatedIssues, issues.size(), page, pageSize);
        } catch (final HttpClientErrorException e) {
            log.error("Error calling Sentry API: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.SENTRY_API_ERROR);
        }
    }

    public SentryIssueDTO getIssueById(final String issueId) {
        try {
            final var headers = createHeaders();
            final var url =
                    sentryProperties.getBaseUrl()
                            + ISSUE_DETAIL_ENDPOINT
                                    .replace("{organization}", sentryProperties.getOrganization())
                                    .replace("{project}", sentryProperties.getProject())
                                    .replace("{issueId}", issueId);

            log.debug("Fetching Sentry issue detail from URL: {}", url);

            final ResponseEntity<SentryIssueDTO> response =
                    restTemplate.exchange(
                            url, HttpMethod.GET, new HttpEntity<>(headers), SentryIssueDTO.class);

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new AppException(ErrorCode.SENTRY_ISSUE_NOT_FOUND);
            }

            return response.getBody();
        } catch (final HttpClientErrorException.NotFound e) {
            log.error("Sentry issue not found: {}", issueId, e);
            throw new AppException(ErrorCode.SENTRY_ISSUE_NOT_FOUND);
        } catch (final HttpClientErrorException e) {
            log.error("Error calling Sentry API: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.SENTRY_API_ERROR);
        }
    }

    public List<SentryEventDTO> getIssueEvents(final String issueId, final Integer limit) {
        try {
            final var url = buildEventsUrl(issueId, limit);
            return fetchEventsFromSentry(url);
        } catch (final HttpClientErrorException.NotFound e) {
            log.error("Sentry issue not found: {}", issueId, e);
            throw new AppException(ErrorCode.SENTRY_ISSUE_NOT_FOUND);
        } catch (final HttpClientErrorException e) {
            log.error("Error calling Sentry API: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.SENTRY_API_ERROR);
        }
    }

    private HttpHeaders createHeaders() {
        final var headers = new HttpHeaders();
        headers.set("Authorization", BEARER_PREFIX + sentryProperties.getAuthToken());
        headers.set("Content-Type", "application/json");
        return headers;
    }

    private String buildIssuesUrl(
            final Integer page,
            final Integer pageSize,
            final String status,
            final String level,
            final String query) {
        final var baseUrl =
                sentryProperties.getBaseUrl()
                        + ISSUES_ENDPOINT
                                .replace("{organization}", sentryProperties.getOrganization())
                                .replace("{project}", sentryProperties.getProject());

        final var uriBuilder = UriComponentsBuilder.fromUriString(baseUrl);

        if (status != null && !status.isEmpty()) {
            uriBuilder.queryParam("status", status);
        }

        // Build query string for Sentry API
        final var queryParts = new ArrayList<String>();
        if (level != null && !level.isEmpty()) {
            queryParts.add("level:" + level);
        }
        if (query != null && !query.isEmpty()) {
            queryParts.add(query);
        }
        if (!queryParts.isEmpty()) {
            uriBuilder.queryParam("query", String.join(" ", queryParts));
        }

        return uriBuilder.toUriString();
    }

    private List<SentryIssueDTO> fetchIssuesFromSentry(
            final String status, final String level, final String query) {
        final var headers = createHeaders();
        final var url = buildIssuesUrl(null, null, status, level, query);

        log.debug("Fetching Sentry issues from URL: {}", url);

        final ResponseEntity<List<SentryIssueDTO>> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        new ParameterizedTypeReference<List<SentryIssueDTO>>() {});

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new AppException(ErrorCode.SENTRY_API_ERROR);
        }

        return response.getBody();
    }

    private List<SentryIssueDTO> applyPagination(
            final List<SentryIssueDTO> issues, final Integer page, final Integer pageSize) {
        final int startIndex = (page - 1) * pageSize;
        final int endIndex = Math.min(startIndex + pageSize, issues.size());
        return startIndex < issues.size()
                ? issues.subList(startIndex, endIndex)
                : new ArrayList<>();
    }

    private SentryIssueListResponseDTO buildIssueListResponse(
            final List<SentryIssueDTO> paginatedIssues,
            final Integer total,
            final Integer page,
            final Integer pageSize) {
        return SentryIssueListResponseDTO.builder()
                .issues(paginatedIssues)
                .total(total)
                .page(page)
                .pageSize(pageSize)
                .build();
    }

    private String buildEventsUrl(final String issueId, final Integer limit) {
        final var baseUrl =
                sentryProperties.getBaseUrl()
                        + EVENTS_ENDPOINT
                                .replace("{organization}", sentryProperties.getOrganization())
                                .replace("{project}", sentryProperties.getProject());

        final var uriBuilder = UriComponentsBuilder.fromUriString(baseUrl);
        uriBuilder.queryParam("query", "issue.id:" + issueId);
        if (limit != null && limit > 0) {
            uriBuilder.queryParam("per_page", String.valueOf(limit));
        }

        return uriBuilder.toUriString();
    }

    private List<SentryEventDTO> fetchEventsFromSentry(final String url) {
        final var headers = createHeaders();
        log.debug("Fetching Sentry events from URL: {}", url);

        final ResponseEntity<List<SentryEventDTO>> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        new ParameterizedTypeReference<List<SentryEventDTO>>() {});

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new AppException(ErrorCode.SENTRY_API_ERROR);
        }

        return response.getBody();
    }
}
