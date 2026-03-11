package com.addressbook.repository;

import com.addressbook.model.Contact;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcContactRepository {
    private static final RowMapper<Contact> CONTACT_ROW_MAPPER = JdbcContactRepository::mapRow;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcContactRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Contact> findAll(String bookName) {
        if (bookName == null || bookName.isBlank()) {
            String sql = """
                SELECT * FROM contacts
                ORDER BY id ASC
                """;
            return jdbcTemplate.query(sql, Map.of(), CONTACT_ROW_MAPPER);
        }

        String sql = """
            SELECT * FROM contacts
            WHERE book_name = :bookName
            ORDER BY id ASC
            """;
        return jdbcTemplate.query(sql, Map.of("bookName", bookName), CONTACT_ROW_MAPPER);
    }

    public List<String> findAllAddressBooks() {
        String sql = """
            SELECT name FROM address_books
            UNION
            SELECT DISTINCT book_name AS name FROM contacts
            ORDER BY name
            """;
        return jdbcTemplate.query(sql, Map.of(), (rs, rowNum) -> rs.getString("name"));
    }

    public void createAddressBook(String name) {
        String existsSql = "SELECT COUNT(*) FROM address_books WHERE name = :name";
        Long exists = jdbcTemplate.queryForObject(existsSql, Map.of("name", name), Long.class);
        if (exists != null && exists > 0) {
            return;
        }
        String insertSql = "INSERT INTO address_books (name) VALUES (:name)";
        try {
            jdbcTemplate.update(insertSql, Map.of("name", name));
        } catch (DataIntegrityViolationException ignored) {
            // Concurrent inserts can race; duplicate key means the row already exists.
        }
    }

    public Contact findById(Long id) {
        String sql = "SELECT * FROM contacts WHERE id = :id";
        List<Contact> contacts = jdbcTemplate.query(sql, Map.of("id", id), CONTACT_ROW_MAPPER);
        return contacts.isEmpty() ? null : contacts.get(0);
    }

    public Contact insert(String bookName, Contact contact) {
        String sql = """
            INSERT INTO contacts (
                book_name, first_name, last_name, address, city, state, zip, phone, email, date_added
            ) VALUES (
                :bookName, :firstName, :lastName, :address, :city, :state, :zip, :phone, :email, :dateAdded
            )
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime dateAdded = contact.getDateAdded() == null ? LocalDateTime.now() : contact.getDateAdded();
        MapSqlParameterSource params = contactParams(bookName, contact).addValue("dateAdded", Timestamp.valueOf(dateAdded));
        jdbcTemplate.update(sql, params, keyHolder, new String[] { "id" });
        Number generatedId = keyHolder.getKey();
        contact.setId(generatedId == null ? null : generatedId.longValue());
        contact.setDateAdded(dateAdded);
        return contact;
    }

    public Contact update(Long id, Contact contact) {
        String sql = """
            UPDATE contacts
            SET first_name = :firstName,
                last_name = :lastName,
                address = :address,
                city = :city,
                state = :state,
                zip = :zip,
                phone = :phone,
                email = :email
            WHERE id = :id
            """;
        MapSqlParameterSource params = contactParams(null, contact).addValue("id", id);
        jdbcTemplate.update(sql, params);
        contact.setId(id);
        return contact;
    }

    public int delete(Long id) {
        String sql = "DELETE FROM contacts WHERE id = :id";
        return jdbcTemplate.update(sql, Map.of("id", id));
    }

    public List<Contact> findByDateRange(LocalDateTime from, LocalDateTime to) {
        String sql = """
            SELECT * FROM contacts
            WHERE date_added BETWEEN :fromDate AND :toDate
            ORDER BY date_added ASC
            """;
        return jdbcTemplate.query(
            sql,
            Map.of("fromDate", Timestamp.valueOf(from), "toDate", Timestamp.valueOf(to)),
            CONTACT_ROW_MAPPER
        );
    }

    public long countByCity(String city) {
        String sql = "SELECT COUNT(*) FROM contacts WHERE city = :city";
        Long count = jdbcTemplate.queryForObject(sql, Map.of("city", city), Long.class);
        return count == null ? 0 : count;
    }

    public long countByState(String state) {
        String sql = "SELECT COUNT(*) FROM contacts WHERE state = :state";
        Long count = jdbcTemplate.queryForObject(sql, Map.of("state", state), Long.class);
        return count == null ? 0 : count;
    }

    private MapSqlParameterSource contactParams(String bookName, Contact contact) {
        return new MapSqlParameterSource()
            .addValue("bookName", bookName)
            .addValue("firstName", contact.getFirstName())
            .addValue("lastName", contact.getLastName())
            .addValue("address", contact.getAddress())
            .addValue("city", contact.getCity())
            .addValue("state", contact.getState())
            .addValue("zip", contact.getZip())
            .addValue("phone", contact.getPhone())
            .addValue("email", contact.getEmail());
    }

    private static Contact mapRow(ResultSet rs, int rowNum) throws SQLException {
        Contact contact = new Contact();
        contact.setId(rs.getLong("id"));
        contact.setFirstName(rs.getString("first_name"));
        contact.setLastName(rs.getString("last_name"));
        contact.setAddress(rs.getString("address"));
        contact.setCity(rs.getString("city"));
        contact.setState(rs.getString("state"));
        contact.setZip(rs.getString("zip"));
        contact.setPhone(rs.getString("phone"));
        contact.setEmail(rs.getString("email"));
        Timestamp dateAdded = rs.getTimestamp("date_added");
        contact.setDateAdded(dateAdded == null ? null : dateAdded.toLocalDateTime());
        return contact;
    }
}
