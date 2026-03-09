package com.addressbook.dto;

import com.addressbook.model.Contact;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ContactSearchResult {
    private String addressBook;
    private Contact contact;
}
