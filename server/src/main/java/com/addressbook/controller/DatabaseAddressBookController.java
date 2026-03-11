package com.addressbook.controller;

import com.addressbook.dto.ApiResponse;
import com.addressbook.service.DatabaseContactService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/db/address-books")
public class DatabaseAddressBookController {
    private final DatabaseContactService databaseContactService;

    public DatabaseAddressBookController(DatabaseContactService databaseContactService) {
        this.databaseContactService = databaseContactService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<String>>> getAddressBooks() {
        return ResponseEntity.ok(ApiResponse.success("DB address books retrieved", databaseContactService.getAddressBooks()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createAddressBook(@RequestParam("name") String name) {
        databaseContactService.createAddressBook(name);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("DB address book created", null));
    }
}
