package com.tpanh.backend.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tpanh.backend.exception.AppException;
import com.tpanh.backend.exception.ErrorCode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class ZaloIdentityClient {
    private static final String ZALO_GRAPH_API_URL = "https://graph.zalo.me/v2.0/me";
    private final RestTemplate restTemplate;

    public ZaloIdentityClient(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ZaloUserInfo getUserInfo(final String accessToken) {
        try {
            final var url = ZALO_GRAPH_API_URL + "?access_token=" + accessToken;
            final ResponseEntity<ZaloUserInfo> response =
                    restTemplate.getForEntity(url, ZaloUserInfo.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                final var userInfo = response.getBody();
                // Validate required fields
                if (userInfo.getId() == null || userInfo.getId().isBlank()) {
                    log.warn("Zalo API trả về user info thiếu ID");
                    throw new AppException(ErrorCode.ZALO_AUTH_FAILED);
                }
                return userInfo;
            }

            log.warn(
                    "Không thể lấy thông tin người dùng từ Zalo. Status: {}",
                    response.getStatusCode());
            throw new AppException(ErrorCode.ZALO_AUTH_FAILED);
        } catch (final AppException e) {
            throw e; // Re-throw AppException as-is
        } catch (final RestClientException e) {
            log.error("Lỗi khi gọi API Zalo Graph", e);
            throw new AppException(ErrorCode.ZALO_AUTH_FAILED);
        } catch (final Exception e) {
            log.error("Lỗi không xác định khi xác thực Zalo", e);
            throw new AppException(ErrorCode.ZALO_AUTH_FAILED);
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ZaloUserInfo {
        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("picture")
        private ZaloPicture picture;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ZaloPicture {
        @JsonProperty("data")
        private ZaloPictureData data;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ZaloPictureData {
        @JsonProperty("url")
        private String url;
    }
}
