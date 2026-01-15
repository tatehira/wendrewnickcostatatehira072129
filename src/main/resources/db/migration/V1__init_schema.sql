CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE artists (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE albums (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    release_year INTEGER,
    cover_url VARCHAR(255),
    artist_id UUID NOT NULL,
    FOREIGN KEY (artist_id) REFERENCES artists(id) ON DELETE CASCADE
);

-- Default Admin User (password: admin)
-- BCrypt hash for 'admin' is $2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6.VwUEM17r78/91phnD.Himr6
INSERT INTO users (id, username, password) VALUES 
('c0656a27-0235-4349-8d60-00918019c384', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6.VwUEM17r78/91phnD.Himr6');
