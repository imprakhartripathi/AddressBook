package com.addressbook.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.addressbook.dto.ApiResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DataSourceControllerTests {
    @LocalServerPort
    private int port;

    private RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(org.springframework.http.client.ClientHttpResponse response) {
                return false;
            }
        });
    }

    @Test
    void supportsSourceDiscoveryAndTransfer() {
        restTemplate.postForEntity(baseUrl("/api/contacts"), sampleContact(), ApiResponse.class);

        ResponseEntity<ApiResponse> sourcesResponse = restTemplate.getForEntity(
            baseUrl("/api/data-sources"),
            ApiResponse.class
        );
        assertEquals(HttpStatus.OK, sourcesResponse.getStatusCode());
        List<String> sources = objectMapper.convertValue(
            sourcesResponse.getBody().getData(),
            new TypeReference<List<String>>() {}
        );
        assertTrue(sources.contains("MEMORY"));

        ResponseEntity<ApiResponse> transferResponse = restTemplate.postForEntity(
            baseUrl("/api/data-sources/transfer?from=MEMORY&to=JSON&book=default"),
            null,
            ApiResponse.class
        );
        assertEquals(HttpStatus.OK, transferResponse.getStatusCode());
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }

    private Map<String, Object> sampleContact() {
        return Map.of(
            "firstName", "Source",
            "lastName", "Test",
            "address", "44 Lane",
            "city", "Pune",
            "state", "MH",
            "zip", "001122",
            "phone", "9999999998",
            "email", "source@test.com"
        );
    }
}
