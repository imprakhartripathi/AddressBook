package com.addressbook.service.datasource;

import com.addressbook.model.Contact;
import com.addressbook.model.DataSourceType;
import com.addressbook.service.ContactService;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MemoryDataSource implements AddressBookDataSource {
    private final ContactService contactService;

    public MemoryDataSource(ContactService contactService) {
        this.contactService = contactService;
    }

    @Override
    public DataSourceType getType() {
        return DataSourceType.MEMORY;
    }

    @Override
    public List<Contact> read(String bookName) {
        return contactService.getAllContacts(bookName);
    }

    @Override
    public List<Contact> write(String bookName, List<Contact> contacts) {
        contactService.replaceContacts(bookName, contacts);
        return contactService.getAllContacts(bookName);
    }
}
