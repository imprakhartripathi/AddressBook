CREATE TABLE IF NOT EXISTS contacts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_name VARCHAR(120) NOT NULL,
    first_name VARCHAR(120),
    last_name VARCHAR(120),
    address VARCHAR(255),
    city VARCHAR(120),
    state VARCHAR(120),
    zip VARCHAR(20),
    phone VARCHAR(40),
    email VARCHAR(255),
    date_added TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
