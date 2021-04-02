-- Password is 'secret'
INSERT INTO users (username, password, first_name, last_name)
VALUES ('test', '$2a$10$TKELuy2B9RK74p.K8Fg7Iu/Xblosk/h5wPaIBGHN8LIrEF/IV3XOy', 'John', 'Doe');

INSERT INTO authorities (username, authority) VALUES ('test', 'ROLE_USER');
INSERT INTO authorities (username, authority) VALUES ('test', 'ROLE_HAMLET');
