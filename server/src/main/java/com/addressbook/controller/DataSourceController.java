package com.addressbook.controller;

import com.addressbook.dto.ApiResponse;
import com.addressbook.model.Contact;
import com.addressbook.model.DataSourceType;
import com.addressbook.service.datasource.DataSourceOrchestratorService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/data-sources")
public class DataSourceController {
    private final DataSourceOrchestratorService orchestratorService;

    public DataSourceController(DataSourceOrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DataSourceType>>> getSources() {
        return ResponseEntity.ok(ApiResponse.success("Data sources retrieved", orchestratorService.availableSources()));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<List<Contact>>> transfer(
        @RequestParam("from") DataSourceType from,
        @RequestParam("to") DataSourceType to,
        @RequestParam(value = "book", required = false) String book
    ) {
        List<Contact> contacts = orchestratorService.transfer(from, to, book);
        return ResponseEntity.ok(ApiResponse.success("Transfer completed", contacts));
    }
}
