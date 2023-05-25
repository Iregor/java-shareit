CREATE TABLE IF NOT EXISTS users ( 
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, 
    name varchar(255) NOT NULL, 
    email varchar(512) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS items (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, 
    name varchar(255) NOT NULL,
    description varchar(1024) NOT NULL,
    available boolean NOT NULL, 
    owner_id BIGINT REFERENCES users(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    item_id BIGINT REFERENCES items(id) NOT NULL,
    booker_id BIGINT REFERENCES users(id) NOT NULL,
    start TIMESTAMP NOT NULL,
    finish TIMESTAMP NOT NULL,
    status varchar(255)
    );

CREATE TABLE IF NOT EXISTS comments (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    item_id BIGINT REFERENCES items(id) NOT NULL,
    author_id BIGINT REFERENCES users(id) NOT NULL,
    text varchar(1024) NOT NULL,
    created TIMESTAMP NOT NULL
    );