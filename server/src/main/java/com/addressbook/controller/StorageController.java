package com.addressbook.controller;

import com.addressbook.dto.ApiResponse;
import com.addressbook.model.Contact;
import com.addressbook.service.ContactService;
import com.addressbook.service.ContactStorageService;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
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
    private final ExecutorService executorService;

    public StorageController(
        ContactService contactService,
        ContactStorageService storageService,
        ExecutorService executorService
    ) {
        this.contactService = contactService;
        this.storageService = storageService;
        this.executorService = executorService;
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

    @PostMapping("/file/export/async")
    public ResponseEntity<ApiResponse<Void>> exportFileAsync(@RequestParam(value = "book", required = false) String book) {
        CompletableFuture.runAsync(() -> {
            List<Contact> contacts = contactService.getAllContacts(book);
            storageService.writeToFile(book, contacts);
        }, executorService);
        return ResponseEntity.accepted().body(ApiResponse.success("Async file export started", null));
    }

    @PostMapping("/file/import/async")
    public ResponseEntity<ApiResponse<Void>> importFileAsync(@RequestParam(value = "book", required = false) String book) {
        CompletableFuture.runAsync(() -> {
            List<Contact> contacts = storageService.readFromFile(book);
            contactService.replaceContacts(book, contacts);
        }, executorService);
        return ResponseEntity.accepted().body(ApiResponse.success("Async file import started", null));
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

    @PostMapping("/csv/export/async")
    public ResponseEntity<ApiResponse<Void>> exportCsvAsync(@RequestParam(value = "book", required = false) String book) {
        CompletableFuture.runAsync(() -> {
            List<Contact> contacts = contactService.getAllContacts(book);
            storageService.writeToCsv(book, contacts);
        }, executorService);
        return ResponseEntity.accepted().body(ApiResponse.success("Async CSV export started", null));
    }

    @PostMapping("/csv/import/async")
    public ResponseEntity<ApiResponse<Void>> importCsvAsync(@RequestParam(value = "book", required = false) String book) {
        CompletableFuture.runAsync(() -> {
            List<Contact> contacts = storageService.readFromCsv(book);
            contactService.replaceContacts(book, contacts);
        }, executorService);
        return ResponseEntity.accepted().body(ApiResponse.success("Async CSV import started", null));
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

    @PostMapping("/json/export/async")
    public ResponseEntity<ApiResponse<Void>> exportJsonAsync(@RequestParam(value = "book", required = false) String book) {
        CompletableFuture.runAsync(() -> {
            List<Contact> contacts = contactService.getAllContacts(book);
            storageService.writeToJson(book, contacts);
        }, executorService);
        return ResponseEntity.accepted().body(ApiResponse.success("Async JSON export started", null));
    }

    @PostMapping("/json/import/async")
    public ResponseEntity<ApiResponse<Void>> importJsonAsync(@RequestParam(value = "book", required = false) String book) {
        CompletableFuture.runAsync(() -> {
            List<Contact> contacts = storageService.readFromJson(book);
            contactService.replaceContacts(book, contacts);
        }, executorService);
        return ResponseEntity.accepted().body(ApiResponse.success("Async JSON import started", null));
    }
}
