package com.addressbook.service;

import com.addressbook.dto.ContactSearchResult;
import com.addressbook.model.Contact;
import java.util.List;
import java.util.Map;

public interface ContactService {
    String DEFAULT_BOOK = "default";

    default List<Contact> getAllContacts() {
        return getAllContacts(DEFAULT_BOOK);
    }

    List<Contact> getAllContacts(String bookName);

    default Contact getContactById(Long id) {
        return getContactById(DEFAULT_BOOK, id);
    }

    Contact getContactById(String bookName, Long id);

    default Contact createContact(Contact contact) {
        return createContact(DEFAULT_BOOK, contact);
    }

    Contact createContact(String bookName, Contact contact);

    default Contact updateContact(Long id, Contact contact) {
        return updateContact(DEFAULT_BOOK, id, contact);
    }

    Contact updateContact(String bookName, Long id, Contact contact);

    default void deleteContact(Long id) {
        deleteContact(DEFAULT_BOOK, id);
    }

    void deleteContact(String bookName, Long id);

    List<Contact> addContacts(String bookName, List<Contact> contacts);

    Map<String, List<Contact>> getAllAddressBooks();

    List<String> getAddressBookNames();

    void createAddressBook(String bookName);

    void replaceContacts(String bookName, List<Contact> contacts);

    List<ContactSearchResult> searchByCityOrState(String city, String state);

    Map<String, List<Contact>> groupByCity();

    Map<String, List<Contact>> groupByState();

    Map<String, Long> countByCity();

    Map<String, Long> countByState();

    List<Contact> sortContacts(String bookName, String sortBy);
}
