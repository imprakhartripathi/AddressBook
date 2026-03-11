package com.addressbook.service.datasource;

import com.addressbook.model.Contact;
import com.addressbook.model.DataSourceType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DataSourceOrchestratorService {
    private final Map<DataSourceType, AddressBookDataSource> dataSources = new EnumMap<>(DataSourceType.class);

    public DataSourceOrchestratorService(List<AddressBookDataSource> sourceList) {
        for (AddressBookDataSource source : sourceList) {
            dataSources.put(source.getType(), source);
        }
    }

    public List<Contact> transfer(DataSourceType from, DataSourceType to, String bookName) {
        AddressBookDataSource fromSource = getSource(from);
        AddressBookDataSource toSource = getSource(to);
        List<Contact> contacts = fromSource.read(bookName);
        return toSource.write(bookName, contacts);
    }

    public List<DataSourceType> availableSources() {
        return dataSources.keySet().stream().sorted().toList();
    }

    private AddressBookDataSource getSource(DataSourceType type) {
        AddressBookDataSource source = dataSources.get(type);
        if (source == null) {
            throw new IllegalArgumentException("Unsupported source: " + type);
        }
        return source;
    }
}
