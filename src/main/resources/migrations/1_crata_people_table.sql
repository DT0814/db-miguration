CREATE TABLE people
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    sex  VARCHAR(255) NOT NULL default 'man'
);
