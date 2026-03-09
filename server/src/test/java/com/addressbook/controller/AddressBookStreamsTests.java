package com.addressbook.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
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
class AddressBookStreamsTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void supportsSearchGroupCountAndSort() throws Exception {
        mockMvc.perform(post("/api/contacts")
                .param("book", "Work")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contact("Ada", "Lovelace", "Pune", "MH"))))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/contacts")
                .param("book", "Personal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contact("Grace", "Hopper", "Delhi", "DL"))))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/contacts/search").param("city", "Pune"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(1)));

        mockMvc.perform(get("/api/contacts/by-city"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.Pune", hasSize(1)));

        mockMvc.perform(get("/api/contacts/count-by-state"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.MH").value(1));

        mockMvc.perform(get("/api/contacts").param("sortBy", "name"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    void supportsBulkCreate() throws Exception {
        List<Map<String, Object>> payload = List.of(
            contact("Alan", "Turing", "London", "LDN"),
            contact("Katherine", "Johnson", "Mumbai", "MH")
        );

        mockMvc.perform(post("/api/contacts/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data", hasSize(2)));
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
