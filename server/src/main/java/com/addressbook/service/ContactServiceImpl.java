package com.addressbook.service;

import com.addressbook.dto.ContactSearchResult;
import com.addressbook.exception.ResourceNotFoundException;
import com.addressbook.model.Contact;
import com.addressbook.util.IdGenerator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ContactServiceImpl implements ContactService {
    private static final Logger log = LoggerFactory.getLogger(ContactServiceImpl.class);
    private static final String SORT_NAME = "name";
    private static final String SORT_CITY = "city";
    private static final String SORT_STATE = "state";
    private static final String SORT_ZIP = "zip";

    private final Map<String, List<Contact>> addressBooks = new ConcurrentHashMap<>();

    public ContactServiceImpl() {
        addressBooks.put(DEFAULT_BOOK, new CopyOnWriteArrayList<>());
    }

    @Override
    public List<Contact> getAllContacts(String bookName) {
        return new ArrayList<>(getBook(bookName));
    }

    @Override
    public Contact getContactById(String bookName, Long id) {
        return getBook(bookName).stream()
            .filter(contact -> contact.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Contact not found: " + id));
    }

    @Override
    public Contact createContact(String bookName, Contact contact) {
        validateContact(contact);
        List<Contact> contacts = getBook(bookName);
        ensureNoDuplicate(contacts, contact.getFirstName(), contact.getLastName(), null);
        Contact newContact = copyContact(contact);
        newContact.setId(IdGenerator.nextId());
        contacts.add(newContact);
        log.info("Created contact with id={} in book={}", newContact.getId(), bookName);
        return newContact;
    }

    @Override
    public Contact updateContact(String bookName, Long id, Contact contact) {
        validateContact(contact);
        List<Contact> contacts = getBook(bookName);
        Contact existing = getContactById(bookName, id);
        ensureNoDuplicate(contacts, contact.getFirstName(), contact.getLastName(), id);
        existing.setFirstName(contact.getFirstName());
        existing.setLastName(contact.getLastName());
        existing.setAddress(contact.getAddress());
        existing.setCity(contact.getCity());
        existing.setState(contact.getState());
        existing.setZip(contact.getZip());
        existing.setPhone(contact.getPhone());
        existing.setEmail(contact.getEmail());
        log.info("Updated contact with id={} in book={}", id, bookName);
        return existing;
    }

    @Override
    public void deleteContact(String bookName, Long id) {
        List<Contact> contacts = getBook(bookName);
        Contact existing = getContactById(bookName, id);
        contacts.remove(existing);
        log.info("Deleted contact with id={} in book={}", id, bookName);
    }

    @Override
    public List<Contact> addContacts(String bookName, List<Contact> contacts) {
        if (contacts == null || contacts.isEmpty()) {
            throw new IllegalArgumentException("Contacts list is required");
        }
        List<Contact> created = new ArrayList<>();
        for (Contact contact : contacts) {
            created.add(createContact(bookName, contact));
        }
        return created;
    }

    @Override
    public Map<String, List<Contact>> getAllAddressBooks() {
        return addressBooks;
    }

    @Override
    public List<String> getAddressBookNames() {
        return new ArrayList<>(addressBooks.keySet());
    }

    @Override
    public void createAddressBook(String bookName) {
        if (isBlank(bookName)) {
            throw new IllegalArgumentException("Address book name is required");
        }
        addressBooks.computeIfAbsent(bookName, name -> new CopyOnWriteArrayList<>());
        log.info("Address book ensured: {}", bookName);
    }

    @Override
    public void replaceContacts(String bookName, List<Contact> contacts) {
        String resolved = isBlank(bookName) ? DEFAULT_BOOK : bookName;
        List<Contact> safeContacts = new CopyOnWriteArrayList<>();
        if (contacts != null) {
            contacts.forEach(contact -> {
                if (contact.getId() == null) {
                    contact.setId(IdGenerator.nextId());
                }
                safeContacts.add(contact);
            });
        }
        addressBooks.put(resolved, safeContacts);
        log.info("Replaced contacts in book={} with {} entries", resolved, safeContacts.size());
    }

    @Override
    public List<ContactSearchResult> searchByCityOrState(String city, String state) {
        boolean hasCity = !isBlank(city);
        boolean hasState = !isBlank(state);
        if (!hasCity && !hasState) {
            throw new IllegalArgumentException("City or state is required");
        }
        return addressBooks.entrySet().stream()
            .flatMap(entry -> entry.getValue().stream()
                .filter(contact -> matchesCityOrState(contact, city, state, hasCity, hasState))
                .map(contact -> new ContactSearchResult(entry.getKey(), contact)))
            .collect(Collectors.toList());
    }

    @Override
    public Map<String, List<Contact>> groupByCity() {
        return flattenContacts().stream()
            .filter(contact -> !isBlank(contact.getCity()))
            .collect(Collectors.groupingBy(Contact::getCity));
    }

    @Override
    public Map<String, List<Contact>> groupByState() {
        return flattenContacts().stream()
            .filter(contact -> !isBlank(contact.getState()))
            .collect(Collectors.groupingBy(Contact::getState));
    }

    @Override
    public Map<String, Long> countByCity() {
        return flattenContacts().stream()
            .filter(contact -> !isBlank(contact.getCity()))
            .collect(Collectors.groupingBy(Contact::getCity, Collectors.counting()));
    }

    @Override
    public Map<String, Long> countByState() {
        return flattenContacts().stream()
            .filter(contact -> !isBlank(contact.getState()))
            .collect(Collectors.groupingBy(Contact::getState, Collectors.counting()));
    }

    @Override
    public List<Contact> sortContacts(String bookName, String sortBy) {
        Comparator<Contact> comparator = comparatorFor(sortBy);
        return getBook(bookName).stream()
            .sorted(comparator)
            .collect(Collectors.toList());
    }

    private Comparator<Contact> comparatorFor(String sortBy) {
        String key = sortBy == null ? SORT_NAME : sortBy.toLowerCase();
        return switch (key) {
            case SORT_CITY -> Comparator.comparing(Contact::getCity, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(Contact::getFirstName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(Contact::getLastName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case SORT_STATE -> Comparator.comparing(Contact::getState, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(Contact::getFirstName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(Contact::getLastName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case SORT_ZIP -> Comparator.comparing(Contact::getZip, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(Contact::getFirstName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(Contact::getLastName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case SORT_NAME -> Comparator.comparing(Contact::getFirstName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(Contact::getLastName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            default -> throw new IllegalArgumentException("Invalid sort option: " + sortBy);
        };
    }

    private List<Contact> flattenContacts() {
        return addressBooks.values().stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    private List<Contact> getBook(String bookName) {
        String resolved = isBlank(bookName) ? DEFAULT_BOOK : bookName;
        return addressBooks.computeIfAbsent(resolved, name -> new CopyOnWriteArrayList<>());
    }

    private void validateContact(Contact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("Contact is required");
        }
        if (isBlank(contact.getEmail())) {
            throw new IllegalArgumentException("Email is required");
        }
        if (isBlank(contact.getPhone())) {
            throw new IllegalArgumentException("Phone is required");
        }
    }

    private void ensureNoDuplicate(List<Contact> contacts, String firstName, String lastName, Long excludeId) {
        boolean exists = contacts.stream()
            .anyMatch(contact ->
                equalsIgnoreCase(contact.getFirstName(), firstName)
                    && equalsIgnoreCase(contact.getLastName(), lastName)
                    && !Objects.equals(contact.getId(), excludeId));
        if (exists) {
            throw new IllegalArgumentException("Duplicate contact name in address book");
        }
    }

    private boolean matchesCityOrState(Contact contact, String city, String state, boolean hasCity, boolean hasState) {
        boolean cityMatch = hasCity && equalsIgnoreCase(contact.getCity(), city);
        boolean stateMatch = hasState && equalsIgnoreCase(contact.getState(), state);
        return cityMatch || stateMatch;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean equalsIgnoreCase(String left, String right) {
        return left != null && right != null && left.equalsIgnoreCase(right);
    }

    private Contact copyContact(Contact contact) {
        Contact copy = new Contact();
        copy.setFirstName(contact.getFirstName());
        copy.setLastName(contact.getLastName());
        copy.setAddress(contact.getAddress());
        copy.setCity(contact.getCity());
        copy.setState(contact.getState());
        copy.setZip(contact.getZip());
        copy.setPhone(contact.getPhone());
        copy.setEmail(contact.getEmail());
        return copy;
    }
}
