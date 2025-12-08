package com.tpanh.backend.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class ZaloIdentityClientTest {
    private static final String ACCESS_TOKEN = "test-access-token";
    private static final String ZALO_ID = "zalo-id-123";
    private static final String ZALO_NAME = "Zalo User Name";

    @Mock private RestTemplate restTemplate;

    private ZaloIdentityClient zaloIdentityClient;

    @BeforeEach
    void setUp() {
        zaloIdentityClient = new ZaloIdentityClient(restTemplate);
    }

    @Test
    void getUserInfo_WithValidToken_ShouldReturnUserInfo() {
        // Given
        final var zaloUserInfo = new ZaloIdentityClient.ZaloUserInfo();
        zaloUserInfo.setId(ZALO_ID);
        zaloUserInfo.setName(ZALO_NAME);

        final var responseEntity = new ResponseEntity<>(zaloUserInfo, HttpStatus.OK);

        when(restTemplate.getForEntity(anyString(), eq(ZaloIdentityClient.ZaloUserInfo.class)))
                .thenReturn(responseEntity);

        // When
        final var result = zaloIdentityClient.getUserInfo(ACCESS_TOKEN);

        // Then
        assertNotNull(result);
        assertEquals(ZALO_ID, result.getId());
        assertEquals(ZALO_NAME, result.getName());
        verify(restTemplate).getForEntity(anyString(), eq(ZaloIdentityClient.ZaloUserInfo.class));
    }

    @Test
    void getUserInfo_WithInvalidResponse_ShouldThrowException() {
        // Given
        final var responseEntity =
                new ResponseEntity<ZaloIdentityClient.ZaloUserInfo>(
                        (ZaloIdentityClient.ZaloUserInfo) null, HttpStatus.BAD_REQUEST);

        when(restTemplate.getForEntity(anyString(), eq(ZaloIdentityClient.ZaloUserInfo.class)))
                .thenReturn(responseEntity);

        // When & Then
        assertThrows(RuntimeException.class, () -> zaloIdentityClient.getUserInfo(ACCESS_TOKEN));
        verify(restTemplate).getForEntity(anyString(), eq(ZaloIdentityClient.ZaloUserInfo.class));
    }

    @Test
    void getUserInfo_WithRestClientException_ShouldThrowException() {
        // Given
        when(restTemplate.getForEntity(anyString(), eq(ZaloIdentityClient.ZaloUserInfo.class)))
                .thenThrow(new RestClientException("Connection failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> zaloIdentityClient.getUserInfo(ACCESS_TOKEN));
        verify(restTemplate).getForEntity(anyString(), eq(ZaloIdentityClient.ZaloUserInfo.class));
    }
}
