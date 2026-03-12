package com.addressbook.service;

import com.addressbook.model.Contact;
import com.addressbook.util.ContactFileMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ContactStorageServiceImpl implements ContactStorageService {
    private static final Logger log = LoggerFactory.getLogger(ContactStorageServiceImpl.class);
    private static final String[] CSV_HEADER = {
        "firstName",
        "lastName",
        "address",
        "city",
        "state",
        "zip",
        "phone",
        "email"
    };

    private final Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, (com.google.gson.JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
            new com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
        .registerTypeAdapter(LocalDateTime.class, (com.google.gson.JsonDeserializer<LocalDateTime>) (json, type, context) ->
            LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        .create();
    private final Path baseDir = Path.of("data");

    @Override
    public List<Contact> readFromFile(String bookName) {
        Path file = ensureBaseDir().resolve(fileName(bookName, "txt"));
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        try {
            List<String> lines = Files.readAllLines(file);
            List<Contact> contacts = new ArrayList<>();
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    contacts.add(ContactFileMapper.fromLine(line));
                }
            }
            return contacts;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read file storage", ex);
        }
    }

    @Override
    public void writeToFile(String bookName, List<Contact> contacts) {
        Path file = ensureBaseDir().resolve(fileName(bookName, "txt"));
        try {
            List<String> lines = new ArrayList<>();
            for (Contact contact : contacts) {
                lines.add(ContactFileMapper.toLine(contact));
            }
            Files.write(file, lines);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to write file storage", ex);
        }
    }

    @Override
    public List<Contact> readFromCsv(String bookName) {
        Path file = ensureBaseDir().resolve(fileName(bookName, "csv"));
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        try (Reader reader = Files.newBufferedReader(file); CSVReader csvReader = new CSVReader(reader)) {
            List<Contact> contacts = new ArrayList<>();
            String[] row;
            boolean isFirst = true;
            while ((row = csvReader.readNext()) != null) {
                if (isFirst) {
                    isFirst = false;
                    continue;
                }
                contacts.add(fromCsv(row));
            }
            return contacts;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to read CSV storage", ex);
        }
    }

    @Override
    public void writeToCsv(String bookName, List<Contact> contacts) {
        Path file = ensureBaseDir().resolve(fileName(bookName, "csv"));
        try (Writer writer = Files.newBufferedWriter(file); CSVWriter csvWriter = new CSVWriter(writer)) {
            csvWriter.writeNext(CSV_HEADER);
            for (Contact contact : contacts) {
                csvWriter.writeNext(toCsv(contact));
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to write CSV storage", ex);
        }
    }

    @Override
    public List<Contact> readFromJson(String bookName) {
        Path file = ensureBaseDir().resolve(fileName(bookName, "json"));
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        try (Reader reader = Files.newBufferedReader(file)) {
            Type listType = new TypeToken<List<Contact>>() {}.getType();
            List<Contact> contacts = gson.fromJson(reader, listType);
            return contacts == null ? new ArrayList<>() : contacts;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read JSON storage", ex);
        }
    }

    @Override
    public void writeToJson(String bookName, List<Contact> contacts) {
        Path file = ensureBaseDir().resolve(fileName(bookName, "json"));
        try (Writer writer = Files.newBufferedWriter(file)) {
            gson.toJson(contacts, writer);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to write JSON storage", ex);
        }
    }

    private Path ensureBaseDir() {
        try {
            Files.createDirectories(baseDir);
            return baseDir;
        } catch (IOException ex) {
            log.error("Failed to create storage directory", ex);
            throw new IllegalStateException("Failed to initialize storage directory", ex);
        }
    }

    private String fileName(String bookName, String extension) {
        String safeName = bookName == null || bookName.isBlank() ? ContactService.DEFAULT_BOOK : bookName;
        return "addressbook-" + safeName + "." + extension;
    }

    private String[] toCsv(Contact contact) {
        return new String[] {
            safe(contact.getFirstName()),
            safe(contact.getLastName()),
            safe(contact.getAddress()),
            safe(contact.getCity()),
            safe(contact.getState()),
            safe(contact.getZip()),
            safe(contact.getPhone()),
            safe(contact.getEmail())
        };
    }

    private Contact fromCsv(String[] row) {
        Contact contact = new Contact();
        contact.setFirstName(get(row, 0));
        contact.setLastName(get(row, 1));
        contact.setAddress(get(row, 2));
        contact.setCity(get(row, 3));
        contact.setState(get(row, 4));
        contact.setZip(get(row, 5));
        contact.setPhone(get(row, 6));
        contact.setEmail(get(row, 7));
        return contact;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String get(String[] row, int index) {
        return index < row.length ? row[index] : "";
    }
}
