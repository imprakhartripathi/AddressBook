package com.addressbook.model;

import lombok.Data;

@Data
public class Contact {
    private Long id;
    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String state;
    private String zip;
    private String phone;
    private String email;
}
