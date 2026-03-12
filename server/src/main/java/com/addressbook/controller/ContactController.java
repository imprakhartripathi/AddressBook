package com.addressbook.controller;

import com.addressbook.dto.ApiResponse;
import com.addressbook.dto.ContactSearchResult;
import com.addressbook.model.Contact;
import com.addressbook.service.ContactService;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
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
@RequestMapping("/api")
public class ContactController {
    private final ContactService contactService;
    private final ExecutorService executorService;

    public ContactController(ContactService contactService, ExecutorService executorService) {
        this.contactService = contactService;
        this.executorService = executorService;
    }

    @GetMapping("/contacts")
    public ResponseEntity<ApiResponse<List<Contact>>> getContacts(
        @RequestParam(value = "book", required = false) String bookName,
        @RequestParam(value = "sortBy", required = false) String sortBy
    ) {
        List<Contact> contacts = sortBy == null
            ? contactService.getAllContacts(bookName)
            : contactService.sortContacts(bookName, sortBy);
        return ResponseEntity.ok(ApiResponse.success("Contacts retrieved", contacts));
    }

    @GetMapping("/contacts/{id}")
    public ResponseEntity<ApiResponse<Contact>> getContact(
        @PathVariable Long id,
        @RequestParam(value = "book", required = false) String bookName
    ) {
        Contact contact = contactService.getContactById(bookName, id);
        return ResponseEntity.ok(ApiResponse.success("Contact retrieved", contact));
    }

    @PostMapping("/contacts")
    public ResponseEntity<ApiResponse<Contact>> createContact(
        @RequestParam(value = "book", required = false) String bookName,
        @RequestBody Contact contact
    ) {
        Contact created = contactService.createContact(bookName, contact);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Contact created", created));
    }

    @PostMapping("/contacts/bulk")
    public ResponseEntity<ApiResponse<List<Contact>>> createContactsBulk(
        @RequestParam(value = "book", required = false) String bookName,
        @RequestParam(value = "async", required = false, defaultValue = "false") boolean async,
        @RequestBody List<Contact> contacts
    ) {
        if (async) {
            CompletableFuture.runAsync(() -> contactService.addContacts(bookName, contacts), executorService);
            return ResponseEntity.accepted()
                .body(ApiResponse.success("Async bulk contact creation started", null));
        }
        List<Contact> created = contactService.addContacts(bookName, contacts);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Bulk contacts created", created));
    }

    @PutMapping("/contacts/{id}")
    public ResponseEntity<ApiResponse<Contact>> updateContact(
        @PathVariable Long id,
        @RequestParam(value = "book", required = false) String bookName,
        @RequestBody Contact contact
    ) {
        Contact updated = contactService.updateContact(bookName, id, contact);
        return ResponseEntity.ok(ApiResponse.success("Contact updated", updated));
    }

    @DeleteMapping("/contacts/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteContact(
        @PathVariable Long id,
        @RequestParam(value = "book", required = false) String bookName
    ) {
        contactService.deleteContact(bookName, id);
        return ResponseEntity.ok(ApiResponse.success("Contact deleted", null));
    }

    @GetMapping("/contacts/search")
    public ResponseEntity<ApiResponse<List<ContactSearchResult>>> searchContacts(
        @RequestParam(value = "city", required = false) String city,
        @RequestParam(value = "state", required = false) String state
    ) {
        List<ContactSearchResult> results = contactService.searchByCityOrState(city, state);
        return ResponseEntity.ok(ApiResponse.success("Search results", results));
    }

    @GetMapping("/contacts/by-city")
    public ResponseEntity<ApiResponse<Map<String, List<Contact>>>> groupByCity() {
        return ResponseEntity.ok(ApiResponse.success("Contacts grouped by city", contactService.groupByCity()));
    }

    @GetMapping("/contacts/by-state")
    public ResponseEntity<ApiResponse<Map<String, List<Contact>>>> groupByState() {
        return ResponseEntity.ok(ApiResponse.success("Contacts grouped by state", contactService.groupByState()));
    }

    @GetMapping("/contacts/count-by-city")
    public ResponseEntity<ApiResponse<Map<String, Long>>> countByCity() {
        return ResponseEntity.ok(ApiResponse.success("Contact count by city", contactService.countByCity()));
    }

    @GetMapping("/contacts/count-by-state")
    public ResponseEntity<ApiResponse<Map<String, Long>>> countByState() {
        return ResponseEntity.ok(ApiResponse.success("Contact count by state", contactService.countByState()));
    }

    @GetMapping("/address-books")
    public ResponseEntity<ApiResponse<List<String>>> listAddressBooks() {
        return ResponseEntity.ok(ApiResponse.success("Address books retrieved", contactService.getAddressBookNames()));
    }

    @PostMapping("/address-books")
    public ResponseEntity<ApiResponse<Void>> createAddressBook(@RequestParam("name") String name) {
        contactService.createAddressBook(name);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Address book created", null));
    }
}
