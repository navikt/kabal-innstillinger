CREATE TABLE innstillinger.abbreviation
(
    id        UUID primary key,
    nav_ident TEXT NOT NULL,
    short     TEXT NOT NULL,
    long      TEXT NOT NULL
);