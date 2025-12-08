package com.tpanh.backend.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
                return response.getBody();
            }

            log.warn(
                    "Không thể lấy thông tin người dùng từ Zalo. Status: {}",
                    response.getStatusCode());
            throw new RuntimeException("Không thể lấy thông tin người dùng từ Zalo");
        } catch (final RestClientException e) {
            log.error("Lỗi khi gọi API Zalo Graph", e);
            throw new RuntimeException("Xác thực với Zalo thất bại", e);
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
