package com.addressbook.controller;

import com.addressbook.dto.ApiResponse;
import com.addressbook.model.Contact;
import com.addressbook.service.ContactService;
import com.addressbook.service.JsonServerSyncService;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/json-server")
public class JsonServerController {
    private final ContactService contactService;
    private final JsonServerSyncService jsonServerSyncService;
    private final ExecutorService executorService;

    public JsonServerController(
        ContactService contactService,
        JsonServerSyncService jsonServerSyncService,
        ExecutorService executorService
    ) {
        this.contactService = contactService;
        this.jsonServerSyncService = jsonServerSyncService;
        this.executorService = executorService;
    }

    @PostMapping("/pull")
    public ResponseEntity<ApiResponse<List<Contact>>> pullFromJsonServer(
        @RequestParam(value = "book", required = false) String book
    ) {
        List<Contact> contacts = jsonServerSyncService.pullContacts();
        contactService.replaceContacts(book, contacts);
        return ResponseEntity.ok(ApiResponse.success("Contacts synced from JSON server", contacts));
    }

    @PostMapping("/push")
    public ResponseEntity<ApiResponse<List<Contact>>> pushToJsonServer() {
        List<Contact> contacts = contactService.getAllContacts();
        List<Contact> created = jsonServerSyncService.pushContacts(contacts);
        contactService.replaceContacts(ContactService.DEFAULT_BOOK, created);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Contacts synced to JSON server", created));
    }

    @PostMapping("/push/async")
    public ResponseEntity<ApiResponse<Void>> pushToJsonServerAsync() {
        CompletableFuture.runAsync(() -> {
            List<Contact> contacts = contactService.getAllContacts();
            List<Contact> created = jsonServerSyncService.pushContactsAsync(contacts);
            contactService.replaceContacts(ContactService.DEFAULT_BOOK, created);
        }, executorService);
        return ResponseEntity.accepted()
            .body(ApiResponse.success("Async JSON server push started", null));
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<Contact>>> bulkAddToJsonServer(
        @RequestBody List<Contact> contacts,
        @RequestParam(value = "async", required = false, defaultValue = "false") boolean async
    ) {
        if (async) {
            CompletableFuture.runAsync(() -> {
                List<Contact> created = jsonServerSyncService.pushContactsAsync(contacts);
                contactService.replaceContacts(ContactService.DEFAULT_BOOK, created);
            }, executorService);
            return ResponseEntity.accepted()
                .body(ApiResponse.success("Async bulk push started", null));
        }
        List<Contact> created = jsonServerSyncService.pushContacts(contacts);
        contactService.replaceContacts(ContactService.DEFAULT_BOOK, created);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Bulk contacts synced", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Contact>> updateJsonServerContact(
        @PathVariable Long id,
        @RequestBody Contact contact,
        @RequestParam(value = "book", required = false) String book
    ) {
        Contact updated = jsonServerSyncService.updateContact(id, contact);
        List<Contact> contacts = jsonServerSyncService.pullContacts();
        contactService.replaceContacts(book, contacts);
        return ResponseEntity.ok(ApiResponse.success("JSON server contact updated", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteJsonServerContact(
        @PathVariable Long id,
        @RequestParam(value = "book", required = false) String book
    ) {
        jsonServerSyncService.deleteContact(id);
        List<Contact> contacts = jsonServerSyncService.pullContacts();
        contactService.replaceContacts(book, contacts);
        return ResponseEntity.ok(ApiResponse.success("JSON server contact deleted", null));
    }
}
