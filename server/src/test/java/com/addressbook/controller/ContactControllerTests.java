package com.addressbook.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.addressbook.dto.ApiResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ContactControllerTests {
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
    void createAndFetchContact() {
        ResponseEntity<ApiResponse> createResponse = restTemplate.postForEntity(
            baseUrl("/api/contacts"),
            sampleContact(),
            ApiResponse.class
        );

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        ApiResponse createdBody = createResponse.getBody();
        assertNotNull(createdBody);
        assertTrue(createdBody.isSuccess());

        ResponseEntity<ApiResponse> listResponse = restTemplate.getForEntity(
            baseUrl("/api/contacts"),
            ApiResponse.class
        );

        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        ApiResponse listBody = listResponse.getBody();
        List<?> data = objectMapper.convertValue(listBody.getData(), new TypeReference<List<?>>() {});
        assertEquals(1, data.size());
    }

    @Test
    void updateAndDeleteContact() {
        ResponseEntity<ApiResponse> createResponse = restTemplate.postForEntity(
            baseUrl("/api/contacts"),
            sampleContact(),
            ApiResponse.class
        );

        Map<String, Object> createdData = objectMapper.convertValue(
            createResponse.getBody().getData(),
            new TypeReference<Map<String, Object>>() {}
        );
        long id = ((Number) createdData.get("id")).longValue();

        Map<String, Object> updated = sampleContact();
        updated.put("city", "Mumbai");

        ResponseEntity<ApiResponse> updateResponse = restTemplate.exchange(
            baseUrl("/api/contacts/" + id),
            HttpMethod.PUT,
            new HttpEntity<>(updated),
            ApiResponse.class
        );
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

        ResponseEntity<ApiResponse> deleteResponse = restTemplate.exchange(
            baseUrl("/api/contacts/" + id),
            HttpMethod.DELETE,
            HttpEntity.EMPTY,
            ApiResponse.class
        );
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());

        ResponseEntity<ApiResponse> fetchResponse = restTemplate.getForEntity(
            baseUrl("/api/contacts/" + id),
            ApiResponse.class
        );
        assertEquals(HttpStatus.NOT_FOUND, fetchResponse.getStatusCode());
    }

    @Test
    void validatesRequiredFields() {
        Map<String, Object> invalid = sampleContact();
        invalid.put("email", "");

        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
            baseUrl("/api/contacts"),
            invalid,
            ApiResponse.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email is required", response.getBody().getMessage());
    }

    @Test
    void preventsDuplicateContactInBook() {
        restTemplate.postForEntity(baseUrl("/api/contacts"), sampleContact(), ApiResponse.class);

        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
            baseUrl("/api/contacts"),
            sampleContact(),
            ApiResponse.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Duplicate contact name in address book", response.getBody().getMessage());
    }

    @Test
    void createsAddressBook() {
        ResponseEntity<ApiResponse> createResponse = restTemplate.postForEntity(
            baseUrl("/api/address-books?name=Personal"),
            null,
            ApiResponse.class
        );

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertTrue(createResponse.getBody().isSuccess());

        ResponseEntity<ApiResponse> listResponse = restTemplate.getForEntity(
            baseUrl("/api/address-books"),
            ApiResponse.class
        );

        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        List<?> data = objectMapper.convertValue(listResponse.getBody().getData(), new TypeReference<List<?>>() {});
        assertTrue(data.contains("Personal"));
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }

    private Map<String, Object> sampleContact() {
        Map<String, Object> contact = new HashMap<>();
        contact.put("firstName", "Ada");
        contact.put("lastName", "Lovelace");
        contact.put("address", "12 Main");
        contact.put("city", "Pune");
        contact.put("state", "MH");
        contact.put("zip", "411001");
        contact.put("phone", "9999999999");
        contact.put("email", "ada@example.com");
        return contact;
    }
}
