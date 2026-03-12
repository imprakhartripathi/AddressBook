package com.addressbook.service.datasource;

import com.addressbook.model.Contact;
import com.addressbook.model.DataSourceType;
import com.addressbook.service.JsonServerSyncService;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class JsonServerDataSource implements AddressBookDataSource {
    private final JsonServerSyncService jsonServerSyncService;

    public JsonServerDataSource(JsonServerSyncService jsonServerSyncService) {
        this.jsonServerSyncService = jsonServerSyncService;
    }

    @Override
    public DataSourceType getType() {
        return DataSourceType.JSON_SERVER;
    }

    @Override
    public List<Contact> read(String bookName) {
        return jsonServerSyncService.pullContacts();
    }

    @Override
    public List<Contact> write(String bookName, List<Contact> contacts) {
        return jsonServerSyncService.pushContacts(contacts);
    }
}
