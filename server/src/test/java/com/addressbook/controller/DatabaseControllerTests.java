package com.addressbook.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.addressbook.dto.ApiResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
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
class DatabaseControllerTests {
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
    void dbCrudAndRangeAndCountFlow() {
        ResponseEntity<ApiResponse> createResponse = restTemplate.postForEntity(
            baseUrl("/api/db/contacts?book=default"),
            sampleContact("Ada", "Pune", "MH"),
            ApiResponse.class
        );
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

        ResponseEntity<ApiResponse> listResponse = restTemplate.getForEntity(baseUrl("/api/db/contacts?book=default"), ApiResponse.class);
        List<?> contacts = objectMapper.convertValue(listResponse.getBody().getData(), new TypeReference<List<?>>() {});
        assertTrue(contacts.size() >= 1);

        LocalDateTime now = LocalDateTime.now();
        String from = now.minusDays(1).toString();
        String to = now.plusDays(1).toString();
        ResponseEntity<ApiResponse> rangeResponse = restTemplate.getForEntity(
            baseUrl("/api/db/contacts/range?from=" + from + "&to=" + to),
            ApiResponse.class
        );
        assertEquals(HttpStatus.OK, rangeResponse.getStatusCode());

        ResponseEntity<ApiResponse> countResponse = restTemplate.getForEntity(
            baseUrl("/api/db/contacts/count?city=Pune"),
            ApiResponse.class
        );
        Map<String, Integer> countPayload = objectMapper.convertValue(
            countResponse.getBody().getData(),
            new TypeReference<Map<String, Integer>>() {}
        );
        assertTrue(countPayload.get("count") >= 1);
    }

    @Test
    void syncBetweenMemoryAndDb() {
        restTemplate.postForEntity(baseUrl("/api/contacts"), sampleContact("Grace", "Delhi", "DL"), ApiResponse.class);

        ResponseEntity<ApiResponse> syncFromMemory = restTemplate.postForEntity(
            baseUrl("/api/db/contacts/sync/from-memory?book=default"),
            null,
            ApiResponse.class
        );
        assertEquals(HttpStatus.OK, syncFromMemory.getStatusCode());

        ResponseEntity<ApiResponse> syncToMemory = restTemplate.postForEntity(
            baseUrl("/api/db/contacts/sync/to-memory?book=default"),
            null,
            ApiResponse.class
        );
        assertEquals(HttpStatus.OK, syncToMemory.getStatusCode());
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }

    private Map<String, Object> sampleContact(String first, String city, String state) {
        return Map.of(
            "firstName", first,
            "lastName", "Tester",
            "address", "12 Main",
            "city", city,
            "state", state,
            "zip", "000001",
            "phone", "9999999999",
            "email", first.toLowerCase() + "@example.com"
        );
    }
}
