CREATE TABLE BOOKS (
	ID INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 1) PRIMARY KEY,
	VERSION INTEGER NOT NULL,
	TITLE VARCHAR(100) NOT NULL
);