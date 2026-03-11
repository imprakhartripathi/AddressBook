package com.addressbook.service.datasource;

import com.addressbook.model.Contact;
import com.addressbook.model.DataSourceType;
import com.addressbook.service.ContactStorageService;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class JsonDataSource implements AddressBookDataSource {
    private final ContactStorageService storageService;

    public JsonDataSource(ContactStorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    public DataSourceType getType() {
        return DataSourceType.JSON;
    }

    @Override
    public List<Contact> read(String bookName) {
        return storageService.readFromJson(bookName);
    }

    @Override
    public List<Contact> write(String bookName, List<Contact> contacts) {
        storageService.writeToJson(bookName, contacts);
        return contacts;
    }
}
