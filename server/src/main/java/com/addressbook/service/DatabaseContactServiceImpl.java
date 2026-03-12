package com.addressbook.service;

import com.addressbook.exception.ResourceNotFoundException;
import com.addressbook.model.Contact;
import com.addressbook.repository.JdbcContactRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseContactServiceImpl implements DatabaseContactService {
    private static final Logger log = LoggerFactory.getLogger(DatabaseContactServiceImpl.class);

    private final JdbcContactRepository jdbcContactRepository;
    private final ContactService contactService;
    private final ExecutorService executorService;

    public DatabaseContactServiceImpl(
        JdbcContactRepository jdbcContactRepository,
        ContactService contactService,
        ExecutorService executorService
    ) {
        this.jdbcContactRepository = jdbcContactRepository;
        this.contactService = contactService;
        this.executorService = executorService;
    }

    @Override
    public List<Contact> getAllContacts(String bookName) {
        return jdbcContactRepository.findAll(bookName);
    }

    @Override
    public Contact getContactById(Long id) {
        Contact contact = jdbcContactRepository.findById(id);
        if (contact == null) {
            throw new ResourceNotFoundException("DB contact not found: " + id);
        }
        return contact;
    }

    @Override
    @Transactional
    public Contact createContact(String bookName, Contact contact) {
        validateContact(contact);
        String resolvedBook = resolveBookName(bookName);
        jdbcContactRepository.createAddressBook(resolvedBook);
        Contact toCreate = copyContact(contact);
        return jdbcContactRepository.insert(resolvedBook, toCreate);
    }

    @Override
    @Transactional
    public Contact updateContact(Long id, Contact contact) {
        validateContact(contact);
        Contact existing = getContactById(id);
        Contact updatePayload = copyContact(contact);
        updatePayload.setDateAdded(existing.getDateAdded());
        return jdbcContactRepository.update(id, updatePayload);
    }

    @Override
    @Transactional
    public void deleteContact(Long id) {
        int deleted = jdbcContactRepository.delete(id);
        if (deleted == 0) {
            throw new ResourceNotFoundException("DB contact not found: " + id);
        }
    }

    @Override
    @Transactional
    public List<Contact> createContactsInBulk(String bookName, List<Contact> contacts) {
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
    public List<Contact> createContactsInBulkAsync(String bookName, List<Contact> contacts) {
        if (contacts == null || contacts.isEmpty()) {
            throw new IllegalArgumentException("Contacts list is required");
        }
        List<CompletableFuture<Contact>> futures = contacts.stream()
            .map(contact -> CompletableFuture.supplyAsync(() -> createContact(bookName, contact), executorService))
            .collect(Collectors.toList());
        return futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
    }

    @Override
    public List<Contact> getContactsByDateRange(LocalDateTime fromDate, LocalDateTime toDate) {
        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException("fromDate and toDate are required");
        }
        return jdbcContactRepository.findByDateRange(fromDate, toDate);
    }

    @Override
    public long countByCity(String city) {
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City is required");
        }
        return jdbcContactRepository.countByCity(city);
    }

    @Override
    public long countByState(String state) {
        if (state == null || state.isBlank()) {
            throw new IllegalArgumentException("State is required");
        }
        return jdbcContactRepository.countByState(state);
    }

    @Override
    @Transactional
    public List<Contact> syncMemoryToDatabase(String bookName) {
        String resolved = resolveBookName(bookName);
        List<Contact> memoryContacts = contactService.getAllContacts(resolved);
        List<Contact> currentDb = jdbcContactRepository.findAll(resolved);
        currentDb.forEach(contact -> jdbcContactRepository.delete(contact.getId()));
        List<Contact> inserted = new ArrayList<>();
        for (Contact contact : memoryContacts) {
            inserted.add(jdbcContactRepository.insert(resolved, copyContact(contact)));
        }
        log.info("Synced {} contacts from memory to DB for book={}", inserted.size(), resolved);
        return inserted;
    }

    @Override
    public List<Contact> syncDatabaseToMemory(String bookName) {
        String resolved = resolveBookName(bookName);
        List<Contact> dbContacts = jdbcContactRepository.findAll(resolved).stream()
            .map(this::copyContact)
            .collect(Collectors.toList());
        contactService.replaceContacts(resolved, dbContacts);
        log.info("Synced {} contacts from DB to memory for book={}", dbContacts.size(), resolved);
        return dbContacts;
    }

    @Override
    public Map<String, Long> syncCheck(Long id) {
        Contact dbContact = getContactById(id);
        Contact memoryContact = contactService.getContactById(id);
        boolean inSync = equalsForSync(dbContact, memoryContact);
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("dbId", dbContact.getId());
        result.put("memoryId", memoryContact.getId());
        result.put("inSync", inSync ? 1L : 0L);
        return result;
    }

    @Override
    public List<String> getAddressBooks() {
        return jdbcContactRepository.findAllAddressBooks();
    }

    @Override
    public void createAddressBook(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Address book name is required");
        }
        jdbcContactRepository.createAddressBook(name.trim());
    }

    private String resolveBookName(String bookName) {
        return (bookName == null || bookName.isBlank()) ? ContactService.DEFAULT_BOOK : bookName;
    }

    private void validateContact(Contact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("Contact is required");
        }
        if (contact.getEmail() == null || contact.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (contact.getPhone() == null || contact.getPhone().isBlank()) {
            throw new IllegalArgumentException("Phone is required");
        }
    }

    private boolean equalsForSync(Contact left, Contact right) {
        return safeEquals(left.getFirstName(), right.getFirstName())
            && safeEquals(left.getLastName(), right.getLastName())
            && safeEquals(left.getAddress(), right.getAddress())
            && safeEquals(left.getCity(), right.getCity())
            && safeEquals(left.getState(), right.getState())
            && safeEquals(left.getZip(), right.getZip())
            && safeEquals(left.getPhone(), right.getPhone())
            && safeEquals(left.getEmail(), right.getEmail());
    }

    private boolean safeEquals(String left, String right) {
        return left == null ? right == null : left.equals(right);
    }

    private Contact copyContact(Contact source) {
        Contact copy = new Contact();
        copy.setId(source.getId());
        copy.setFirstName(source.getFirstName());
        copy.setLastName(source.getLastName());
        copy.setAddress(source.getAddress());
        copy.setCity(source.getCity());
        copy.setState(source.getState());
        copy.setZip(source.getZip());
        copy.setPhone(source.getPhone());
        copy.setEmail(source.getEmail());
        copy.setDateAdded(source.getDateAdded());
        return copy;
    }
}
