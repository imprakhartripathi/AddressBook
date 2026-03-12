package com.addressbook.service;

import com.addressbook.model.Contact;
import java.util.List;

public interface ContactStorageService {
    List<Contact> readFromFile(String bookName);

    void writeToFile(String bookName, List<Contact> contacts);

    List<Contact> readFromCsv(String bookName);

    void writeToCsv(String bookName, List<Contact> contacts);

    List<Contact> readFromJson(String bookName);

    void writeToJson(String bookName, List<Contact> contacts);
}
