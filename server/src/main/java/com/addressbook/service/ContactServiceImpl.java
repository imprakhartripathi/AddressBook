package com.addressbook.service;

import com.addressbook.exception.ResourceNotFoundException;
import com.addressbook.model.Contact;
import com.addressbook.util.IdGenerator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ContactServiceImpl implements ContactService {
    private static final Logger log = LoggerFactory.getLogger(ContactServiceImpl.class);
    private final List<Contact> contacts = new CopyOnWriteArrayList<>();

    @Override
    public List<Contact> getAllContacts() {
        return contacts;
    }

    @Override
    public Contact getContactById(Long id) {
        return contacts.stream()
            .filter(contact -> contact.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Contact not found: " + id));
    }

    @Override
    public Contact createContact(Contact contact) {
        validateContact(contact);
        Contact newContact = copyContact(contact);
        newContact.setId(IdGenerator.nextId());
        contacts.add(newContact);
        log.info("Created contact with id={}", newContact.getId());
        return newContact;
    }

    @Override
    public Contact updateContact(Long id, Contact contact) {
        validateContact(contact);
        Contact existing = getContactById(id);
        existing.setFirstName(contact.getFirstName());
        existing.setLastName(contact.getLastName());
        existing.setAddress(contact.getAddress());
        existing.setCity(contact.getCity());
        existing.setState(contact.getState());
        existing.setZip(contact.getZip());
        existing.setPhone(contact.getPhone());
        existing.setEmail(contact.getEmail());
        log.info("Updated contact with id={}", id);
        return existing;
    }

    @Override
    public void deleteContact(Long id) {
        Contact existing = getContactById(id);
        contacts.remove(existing);
        log.info("Deleted contact with id={}", id);
    }

    private void validateContact(Contact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("Contact is required");
        }
        if (isBlank(contact.getEmail())) {
            throw new IllegalArgumentException("Email is required");
        }
        if (isBlank(contact.getPhone())) {
            throw new IllegalArgumentException("Phone is required");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Contact copyContact(Contact contact) {
        Contact copy = new Contact();
        copy.setFirstName(contact.getFirstName());
        copy.setLastName(contact.getLastName());
        copy.setAddress(contact.getAddress());
        copy.setCity(contact.getCity());
        copy.setState(contact.getState());
        copy.setZip(contact.getZip());
        copy.setPhone(contact.getPhone());
        copy.setEmail(contact.getEmail());
        return copy;
    }
}
