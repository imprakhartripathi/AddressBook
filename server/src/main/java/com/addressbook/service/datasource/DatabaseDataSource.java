package com.addressbook.service.datasource;

import com.addressbook.model.Contact;
import com.addressbook.model.DataSourceType;
import com.addressbook.service.ContactService;
import com.addressbook.service.DatabaseContactService;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DatabaseDataSource implements AddressBookDataSource {
    private final DatabaseContactService databaseContactService;
    private final ContactService contactService;

    public DatabaseDataSource(DatabaseContactService databaseContactService, ContactService contactService) {
        this.databaseContactService = databaseContactService;
        this.contactService = contactService;
    }

    @Override
    public DataSourceType getType() {
        return DataSourceType.DB;
    }

    @Override
    public List<Contact> read(String bookName) {
        return databaseContactService.getAllContacts(bookName);
    }

    @Override
    public List<Contact> write(String bookName, List<Contact> contacts) {
        String resolved = (bookName == null || bookName.isBlank()) ? ContactService.DEFAULT_BOOK : bookName;
        contactService.replaceContacts(resolved, contacts);
        return databaseContactService.syncMemoryToDatabase(resolved);
    }
}
