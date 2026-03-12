package com.addressbook.util;

import com.addressbook.model.Contact;

public final class ContactFileMapper {
    private static final String DELIMITER = "|";

    private ContactFileMapper() {
    }

    public static String toLine(Contact contact) {
        return String.join(DELIMITER,
            safe(contact.getFirstName()),
            safe(contact.getLastName()),
            safe(contact.getAddress()),
            safe(contact.getCity()),
            safe(contact.getState()),
            safe(contact.getZip()),
            safe(contact.getPhone()),
            safe(contact.getEmail()));
    }

    public static Contact fromLine(String line) {
        String[] parts = line.split("\\|", -1);
        Contact contact = new Contact();
        contact.setFirstName(get(parts, 0));
        contact.setLastName(get(parts, 1));
        contact.setAddress(get(parts, 2));
        contact.setCity(get(parts, 3));
        contact.setState(get(parts, 4));
        contact.setZip(get(parts, 5));
        contact.setPhone(get(parts, 6));
        contact.setEmail(get(parts, 7));
        return contact;
    }

    private static String safe(String value) {
        return value == null ? "" : value.replace(DELIMITER, " ");
    }

    private static String get(String[] parts, int index) {
        return index < parts.length ? parts[index] : "";
    }
}
