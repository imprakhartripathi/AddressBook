package com.addressbook.service;

import com.addressbook.model.Contact;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface DatabaseContactService {
    List<Contact> getAllContacts(String bookName);

    Contact getContactById(Long id);

    Contact createContact(String bookName, Contact contact);

    Contact updateContact(Long id, Contact contact);

    void deleteContact(Long id);

    List<Contact> createContactsInBulk(String bookName, List<Contact> contacts);

    List<Contact> createContactsInBulkAsync(String bookName, List<Contact> contacts);

    List<Contact> getContactsByDateRange(LocalDateTime fromDate, LocalDateTime toDate);

    long countByCity(String city);

    long countByState(String state);

    List<Contact> syncMemoryToDatabase(String bookName);

    List<Contact> syncDatabaseToMemory(String bookName);

    Map<String, Long> syncCheck(Long id);
}
