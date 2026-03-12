package com.addressbook.service.datasource;

import com.addressbook.model.Contact;
import com.addressbook.model.DataSourceType;
import com.addressbook.service.ContactStorageService;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FileDataSource implements AddressBookDataSource {
    private final ContactStorageService storageService;

    public FileDataSource(ContactStorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    public DataSourceType getType() {
        return DataSourceType.FILE;
    }

    @Override
    public List<Contact> read(String bookName) {
        return storageService.readFromFile(bookName);
    }

    @Override
    public List<Contact> write(String bookName, List<Contact> contacts) {
        storageService.writeToFile(bookName, contacts);
        return contacts;
    }
}
