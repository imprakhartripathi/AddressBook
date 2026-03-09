package com.addressbook.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ContactControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createAndFetchContact() throws Exception {
        mockMvc.perform(post("/api/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleContact())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists());

        mockMvc.perform(get("/api/contacts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void updateAndDeleteContact() throws Exception {
        String response = mockMvc.perform(post("/api/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleContact())))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        long id = objectMapper.readTree(response).path("data").path("id").asLong();

        Map<String, Object> updated = sampleContact();
        updated.put("city", "Mumbai");

        mockMvc.perform(put("/api/contacts/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.city").value("Mumbai"));

        mockMvc.perform(delete("/api/contacts/{id}", id))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/contacts/{id}", id))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", containsString("Contact not found")));
    }

    @Test
    void validatesRequiredFields() throws Exception {
        Map<String, Object> invalid = sampleContact();
        invalid.put("email", "");

        mockMvc.perform(post("/api/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Email is required"));
    }

    @Test
    void preventsDuplicateContactInBook() throws Exception {
        mockMvc.perform(post("/api/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleContact())))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleContact())))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Duplicate contact name in address book"));
    }

    @Test
    void createsAddressBook() throws Exception {
        mockMvc.perform(post("/api/address-books")
                .param("name", "Personal"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/address-books"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray());
    }

    private Map<String, Object> sampleContact() {
        return Map.of(
            "firstName", "Ada",
            "lastName", "Lovelace",
            "address", "12 Main",
            "city", "Pune",
            "state", "MH",
            "zip", "411001",
            "phone", "9999999999",
            "email", "ada@example.com"
        );
    }
}
