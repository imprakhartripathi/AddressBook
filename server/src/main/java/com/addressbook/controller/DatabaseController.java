package com.addressbook.controller;

import com.addressbook.dto.ApiResponse;
import com.addressbook.model.Contact;
import com.addressbook.service.DatabaseContactService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/db/contacts")
public class DatabaseController {
    private final DatabaseContactService databaseContactService;

    public DatabaseController(DatabaseContactService databaseContactService) {
        this.databaseContactService = databaseContactService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Contact>>> getAllContacts(
        @RequestParam(value = "book", required = false) String bookName
    ) {
        return ResponseEntity.ok(ApiResponse.success("DB contacts retrieved", databaseContactService.getAllContacts(bookName)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Contact>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("DB contact retrieved", databaseContactService.getContactById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Contact>> create(
        @RequestParam(value = "book", required = false) String bookName,
        @RequestBody Contact contact
    ) {
        Contact created = databaseContactService.createContact(bookName, contact);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("DB contact created", created));
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<Contact>>> createBulk(
        @RequestParam(value = "book", required = false) String bookName,
        @RequestParam(value = "async", defaultValue = "false") boolean async,
        @RequestBody List<Contact> contacts
    ) {
        List<Contact> created = async
            ? databaseContactService.createContactsInBulkAsync(bookName, contacts)
            : databaseContactService.createContactsInBulk(bookName, contacts);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(async ? "DB bulk async insert complete" : "DB bulk insert complete", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Contact>> update(@PathVariable Long id, @RequestBody Contact contact) {
        Contact updated = databaseContactService.updateContact(id, contact);
        return ResponseEntity.ok(ApiResponse.success("DB contact updated", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        databaseContactService.deleteContact(id);
        return ResponseEntity.ok(ApiResponse.success("DB contact deleted", null));
    }

    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<Contact>>> getByDateRange(
        @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ResponseEntity.ok(ApiResponse.success("DB contacts retrieved by date range", databaseContactService.getContactsByDateRange(from, to)));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getCount(
        @RequestParam(value = "city", required = false) String city,
        @RequestParam(value = "state", required = false) String state
    ) {
        if ((city == null || city.isBlank()) && (state == null || state.isBlank())) {
            throw new IllegalArgumentException("City or state is required");
        }
        Map<String, Long> payload = state != null && !state.isBlank()
            ? Map.of("count", databaseContactService.countByState(state))
            : Map.of("count", databaseContactService.countByCity(city));
        return ResponseEntity.ok(ApiResponse.success("DB contact count retrieved", payload));
    }

    @PostMapping("/sync/from-memory")
    public ResponseEntity<ApiResponse<List<Contact>>> syncFromMemory(
        @RequestParam(value = "book", required = false) String bookName
    ) {
        return ResponseEntity.ok(ApiResponse.success("Synced memory to DB", databaseContactService.syncMemoryToDatabase(bookName)));
    }

    @PostMapping("/sync/to-memory")
    public ResponseEntity<ApiResponse<List<Contact>>> syncToMemory(
        @RequestParam(value = "book", required = false) String bookName
    ) {
        return ResponseEntity.ok(ApiResponse.success("Synced DB to memory", databaseContactService.syncDatabaseToMemory(bookName)));
    }

    @GetMapping("/{id}/sync-check")
    public ResponseEntity<ApiResponse<Map<String, Long>>> syncCheck(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Sync check complete", databaseContactService.syncCheck(id)));
    }
}
