CREATE TABLE users
(
    username   VARCHAR(64) PRIMARY KEY,
    password   VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name  VARCHAR(255)
);

CREATE TABLE authorities
(
    username  VARCHAR(64) NOT NULL,
    authority VARCHAR(64) NOT NULL,
    CONSTRAINT pk_authorities PRIMARY KEY (username, authority),
    CONSTRAINT fk_authorities_users FOREIGN KEY (username) REFERENCES users (username) ON DELETE CASCADE
);
