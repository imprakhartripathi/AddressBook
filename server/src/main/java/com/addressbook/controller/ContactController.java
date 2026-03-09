package com.addressbook.controller;

import com.addressbook.dto.ApiResponse;
import com.addressbook.model.Contact;
import com.addressbook.service.ContactService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {
    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Contact>>> getContacts() {
        List<Contact> contacts = contactService.getAllContacts();
        return ResponseEntity.ok(ApiResponse.success("Contacts retrieved", contacts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Contact>> getContact(@PathVariable Long id) {
        Contact contact = contactService.getContactById(id);
        return ResponseEntity.ok(ApiResponse.success("Contact retrieved", contact));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Contact>> createContact(@RequestBody Contact contact) {
        Contact created = contactService.createContact(contact);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Contact created", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Contact>> updateContact(
        @PathVariable Long id,
        @RequestBody Contact contact
    ) {
        Contact updated = contactService.updateContact(id, contact);
        return ResponseEntity.ok(ApiResponse.success("Contact updated", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteContact(@PathVariable Long id) {
        contactService.deleteContact(id);
        return ResponseEntity.ok(ApiResponse.success("Contact deleted", null));
    }
}
