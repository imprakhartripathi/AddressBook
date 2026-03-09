package com.addressbook.service;

import com.addressbook.model.Contact;
import java.util.List;

public interface ContactService {
    List<Contact> getAllContacts();

    Contact getContactById(Long id);

    Contact createContact(Contact contact);

    Contact updateContact(Long id, Contact contact);

    void deleteContact(Long id);
}
