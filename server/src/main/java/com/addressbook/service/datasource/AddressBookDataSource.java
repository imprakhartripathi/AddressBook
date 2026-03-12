package com.addressbook.service.datasource;

import com.addressbook.model.Contact;
import com.addressbook.model.DataSourceType;
import java.util.List;

public interface AddressBookDataSource {
    DataSourceType getType();

    List<Contact> read(String bookName);

    List<Contact> write(String bookName, List<Contact> contacts);
}
