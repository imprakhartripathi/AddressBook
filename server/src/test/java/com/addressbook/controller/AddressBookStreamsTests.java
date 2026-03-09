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
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AddressBookStreamsTests {
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
    void supportsSearchGroupCountAndSort() {
        restTemplate.postForEntity(
            baseUrl("/api/contacts?book=Work"),
            contact("Ada", "Lovelace", "Pune", "MH"),
            ApiResponse.class
        );

        restTemplate.postForEntity(
            baseUrl("/api/contacts?book=Personal"),
            contact("Grace", "Hopper", "Delhi", "DL"),
            ApiResponse.class
        );

        ResponseEntity<ApiResponse> searchResponse = restTemplate.getForEntity(
            baseUrl("/api/contacts/search?city=Pune"),
            ApiResponse.class
        );
        List<?> searchData = objectMapper.convertValue(
            searchResponse.getBody().getData(),
            new TypeReference<List<?>>() {}
        );
        assertEquals(1, searchData.size());

        ResponseEntity<ApiResponse> groupResponse = restTemplate.getForEntity(
            baseUrl("/api/contacts/by-city"),
            ApiResponse.class
        );
        Map<String, Object> groupData = objectMapper.convertValue(
            groupResponse.getBody().getData(),
            new TypeReference<Map<String, Object>>() {}
        );
        assertTrue(groupData.containsKey("Pune"));

        ResponseEntity<ApiResponse> countResponse = restTemplate.getForEntity(
            baseUrl("/api/contacts/count-by-state"),
            ApiResponse.class
        );
        Map<String, Integer> countData = objectMapper.convertValue(
            countResponse.getBody().getData(),
            new TypeReference<Map<String, Integer>>() {}
        );
        assertEquals(1, countData.get("MH").intValue());

        ResponseEntity<ApiResponse> sortedResponse = restTemplate.getForEntity(
            baseUrl("/api/contacts?book=Work&sortBy=name"),
            ApiResponse.class
        );
        List<?> sortedData = objectMapper.convertValue(
            sortedResponse.getBody().getData(),
            new TypeReference<List<?>>() {}
        );
        assertEquals(1, sortedData.size());
    }

    @Test
    void supportsBulkCreate() {
        List<Map<String, Object>> payload = List.of(
            contact("Alan", "Turing", "London", "LDN"),
            contact("Katherine", "Johnson", "Mumbai", "MH")
        );

        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
            baseUrl("/api/contacts/bulk"),
            payload,
            ApiResponse.class
        );

        List<?> data = objectMapper.convertValue(response.getBody().getData(), new TypeReference<List<?>>() {});
        assertEquals(2, data.size());
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }

    private Map<String, Object> contact(String first, String last, String city, String state) {
        return Map.of(
            "firstName", first,
            "lastName", last,
            "address", "12 Main",
            "city", city,
            "state", state,
            "zip", "000000",
            "phone", "9999999999",
            "email", first.toLowerCase() + "@example.com"
        );
    }
}
