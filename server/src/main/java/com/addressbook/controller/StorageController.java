package com.addressbook.controller;

import com.addressbook.dto.ApiResponse;
import com.addressbook.model.Contact;
import com.addressbook.service.ContactService;
import com.addressbook.service.ContactStorageService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/storage")
public class StorageController {
    private final ContactService contactService;
    private final ContactStorageService storageService;

    public StorageController(ContactService contactService, ContactStorageService storageService) {
        this.contactService = contactService;
        this.storageService = storageService;
    }

    @PostMapping("/file/export")
    public ResponseEntity<ApiResponse<Void>> exportFile(@RequestParam(value = "book", required = false) String book) {
        List<Contact> contacts = contactService.getAllContacts(book);
        storageService.writeToFile(book, contacts);
        return ResponseEntity.ok(ApiResponse.success("File export completed", null));
    }

    @PostMapping("/file/import")
    public ResponseEntity<ApiResponse<Void>> importFile(@RequestParam(value = "book", required = false) String book) {
        List<Contact> contacts = storageService.readFromFile(book);
        contactService.replaceContacts(book, contacts);
        return ResponseEntity.ok(ApiResponse.success("File import completed", null));
    }

    @PostMapping("/csv/export")
    public ResponseEntity<ApiResponse<Void>> exportCsv(@RequestParam(value = "book", required = false) String book) {
        List<Contact> contacts = contactService.getAllContacts(book);
        storageService.writeToCsv(book, contacts);
        return ResponseEntity.ok(ApiResponse.success("CSV export completed", null));
    }

    @PostMapping("/csv/import")
    public ResponseEntity<ApiResponse<Void>> importCsv(@RequestParam(value = "book", required = false) String book) {
        List<Contact> contacts = storageService.readFromCsv(book);
        contactService.replaceContacts(book, contacts);
        return ResponseEntity.ok(ApiResponse.success("CSV import completed", null));
    }

    @PostMapping("/json/export")
    public ResponseEntity<ApiResponse<Void>> exportJson(@RequestParam(value = "book", required = false) String book) {
        List<Contact> contacts = contactService.getAllContacts(book);
        storageService.writeToJson(book, contacts);
        return ResponseEntity.ok(ApiResponse.success("JSON export completed", null));
    }

    @PostMapping("/json/import")
    public ResponseEntity<ApiResponse<Void>> importJson(@RequestParam(value = "book", required = false) String book) {
        List<Contact> contacts = storageService.readFromJson(book);
        contactService.replaceContacts(book, contacts);
        return ResponseEntity.ok(ApiResponse.success("JSON import completed", null));
    }
}
