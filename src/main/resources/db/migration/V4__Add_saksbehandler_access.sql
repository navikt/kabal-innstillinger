CREATE TABLE innstillinger.saksbehandler_access
(
    saksbehandlerident     TEXT PRIMARY KEY,
    created                TIMESTAMP NOT NULL,
    access_rights_modified TIMESTAMP NOT NULL
);

CREATE TABLE innstillinger.saksbehandler_access_ytelse
(
    saksbehandlerident TEXT NOT NULL,
    ytelse_id          TEXT NOT NULL,
    PRIMARY KEY (saksbehandlerident, ytelse_id),
    FOREIGN KEY (saksbehandlerident)
        REFERENCES innstillinger.saksbehandler_access (saksbehandlerident)
);

CREATE INDEX ytelse_saksbehandlerident_idx ON innstillinger.saksbehandler_access_ytelse (saksbehandlerident);