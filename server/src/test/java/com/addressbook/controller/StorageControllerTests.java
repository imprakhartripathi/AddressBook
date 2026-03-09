package com.addressbook.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.addressbook.dto.ApiResponse;
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
class StorageControllerTests {
    @LocalServerPort
    private int port;

    private RestTemplate restTemplate;

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
    void exportAndImportFileStorage() {
        restTemplate.postForEntity(baseUrl("/api/contacts?book=io"), sampleContact(), ApiResponse.class);

        ResponseEntity<ApiResponse> exportResponse = restTemplate.postForEntity(
            baseUrl("/api/storage/file/export?book=io"),
            null,
            ApiResponse.class
        );

        assertEquals(HttpStatus.OK, exportResponse.getStatusCode());
        assertTrue(exportResponse.getBody().isSuccess());

        ResponseEntity<ApiResponse> importResponse = restTemplate.postForEntity(
            baseUrl("/api/storage/file/import?book=io"),
            null,
            ApiResponse.class
        );

        assertEquals(HttpStatus.OK, importResponse.getStatusCode());
        assertTrue(importResponse.getBody().isSuccess());
    }

    @Test
    void exportAndImportCsvStorage() {
        restTemplate.postForEntity(baseUrl("/api/contacts?book=csv"), sampleContact(), ApiResponse.class);

        ResponseEntity<ApiResponse> exportResponse = restTemplate.postForEntity(
            baseUrl("/api/storage/csv/export?book=csv"),
            null,
            ApiResponse.class
        );

        assertEquals(HttpStatus.OK, exportResponse.getStatusCode());

        ResponseEntity<ApiResponse> importResponse = restTemplate.postForEntity(
            baseUrl("/api/storage/csv/import?book=csv"),
            null,
            ApiResponse.class
        );

        assertEquals(HttpStatus.OK, importResponse.getStatusCode());
    }

    @Test
    void exportAndImportJsonStorage() {
        restTemplate.postForEntity(baseUrl("/api/contacts?book=json"), sampleContact(), ApiResponse.class);

        ResponseEntity<ApiResponse> exportResponse = restTemplate.postForEntity(
            baseUrl("/api/storage/json/export?book=json"),
            null,
            ApiResponse.class
        );

        assertEquals(HttpStatus.OK, exportResponse.getStatusCode());

        ResponseEntity<ApiResponse> importResponse = restTemplate.postForEntity(
            baseUrl("/api/storage/json/import?book=json"),
            null,
            ApiResponse.class
        );

        assertEquals(HttpStatus.OK, importResponse.getStatusCode());
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }

    private Map<String, Object> sampleContact() {
        return Map.of(
            "firstName", "Grace",
            "lastName", "Hopper",
            "address", "22 Navy",
            "city", "Delhi",
            "state", "DL",
            "zip", "110001",
            "phone", "8888888888",
            "email", "grace@example.com"
        );
    }
}
