package com.addressbook.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class StorageControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void exportAndImportFileStorage() throws Exception {
        mockMvc.perform(post("/api/contacts")
                .param("book", "io")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleContact())))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/storage/file/export").param("book", "io"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/api/storage/file/import").param("book", "io"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void exportAndImportCsvStorage() throws Exception {
        mockMvc.perform(post("/api/contacts")
                .param("book", "csv")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleContact())))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/storage/csv/export").param("book", "csv"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/api/storage/csv/import").param("book", "csv"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void exportAndImportJsonStorage() throws Exception {
        mockMvc.perform(post("/api/contacts")
                .param("book", "json")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleContact())))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/storage/json/export").param("book", "json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/api/storage/json/import").param("book", "json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
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
