-- Table for users
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT,
    phone TEXT UNIQUE NOT NULL,
    time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Table for contacts
CREATE TABLE contacts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    number TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE counter (
    name TEXT UNIQUE,
    value INTEGER DEFAULT 0
);

INSERT INTO counter (name, value) VALUES ('unique_user_counter', 0);