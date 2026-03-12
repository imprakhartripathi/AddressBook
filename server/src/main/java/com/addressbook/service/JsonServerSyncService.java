package com.addressbook.service;

import com.addressbook.model.Contact;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class JsonServerSyncService {
    private static final Logger log = LoggerFactory.getLogger(JsonServerSyncService.class);

    private final RestTemplate restTemplate;
    private final ExecutorService executorService;
    private final String baseUrl;

    public JsonServerSyncService(
        RestTemplate restTemplate,
        ExecutorService executorService,
        @Value("${jsonserver.base-url}") String baseUrl
    ) {
        this.restTemplate = restTemplate;
        this.executorService = executorService;
        this.baseUrl = baseUrl;
    }

    public List<Contact> pullContacts() {
        Contact[] response = restTemplate.getForObject(baseUrl, Contact[].class);
        List<Contact> contacts = response == null ? List.of() : Arrays.asList(response);
        log.info("Pulled {} contacts from JSON server", contacts.size());
        return contacts;
    }

    public List<Contact> pushContacts(List<Contact> contacts) {
        if (contacts == null) {
            return List.of();
        }
        List<Contact> created = contacts.stream()
            .map(contact -> restTemplate.postForObject(baseUrl, contact, Contact.class))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        log.info("Pushed {} contacts to JSON server", created.size());
        return created;
    }

    public List<Contact> pushContactsAsync(List<Contact> contacts) {
        if (contacts == null || contacts.isEmpty()) {
            return List.of();
        }
        List<CompletableFuture<Contact>> futures = contacts.stream()
            .map(contact -> CompletableFuture.supplyAsync(
                () -> restTemplate.postForObject(baseUrl, contact, Contact.class),
                executorService))
            .collect(Collectors.toList());
        List<Contact> created = futures.stream()
            .map(CompletableFuture::join)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        log.info("Pushed {} contacts to JSON server using async", created.size());
        return created;
    }

    public Contact updateContact(Long id, Contact contact) {
        String url = baseUrl + "/" + id;
        restTemplate.put(url, contact);
        Contact updated = restTemplate.getForObject(url, Contact.class);
        log.info("Updated contact {} on JSON server", id);
        return updated;
    }

    public void deleteContact(Long id) {
        restTemplate.delete(baseUrl + "/" + id);
        log.info("Deleted contact {} from JSON server", id);
    }
}
