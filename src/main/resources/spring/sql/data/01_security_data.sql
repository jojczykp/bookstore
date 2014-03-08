INSERT INTO USERS (ID, NAME, PASSWORD, NOT_EXPIRED, NOT_LOCKED, CREDENTIALS_NOT_EXPIRED, ENABLED) VALUES (101, 'admin', 'admin', true, true, true, true);
INSERT INTO USERS (ID, NAME, PASSWORD, NOT_EXPIRED, NOT_LOCKED, CREDENTIALS_NOT_EXPIRED, ENABLED) VALUES (102, 'user' , 'user' , true, true, true, true);

INSERT INTO AUTHORITIES (ID, ROLE) VALUES (201, 'ROLE_ADMIN');
INSERT INTO AUTHORITIES (ID, ROLE) VALUES (202, 'ROLE_USER');

INSERT INTO USERS_AUTHORITIES (USER_ID, AUTHORITY_ID) VALUES (101, 201);
INSERT INTO USERS_AUTHORITIES (USER_ID, AUTHORITY_ID) VALUES (101, 202);
INSERT INTO USERS_AUTHORITIES (USER_ID, AUTHORITY_ID) VALUES (102, 202);