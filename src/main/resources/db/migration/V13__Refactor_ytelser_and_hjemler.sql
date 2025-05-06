ALTER TABLE innstillinger.innstillinger
    ALTER COLUMN hjemler drop not null;

ALTER TABLE innstillinger.innstillinger
    ALTER COLUMN ytelser drop not null;

CREATE TABLE innstillinger.innstillinger_hjemmel
(
    id        TEXT NOT NULL,
    innstillinger_saksbehandlerident TEXT NOT NULL,
    PRIMARY KEY (id, innstillinger_saksbehandlerident),
    CONSTRAINT fk_hjemmel_innstillinger
        FOREIGN KEY (innstillinger_saksbehandlerident)
            REFERENCES innstillinger.innstillinger (saksbehandlerident)
);

CREATE TABLE innstillinger.innstillinger_ytelse
(
    id        TEXT NOT NULL,
    innstillinger_saksbehandlerident TEXT NOT NULL,
    PRIMARY KEY (id, innstillinger_saksbehandlerident),
    CONSTRAINT fk_ytelse_innstillinger
        FOREIGN KEY (innstillinger_saksbehandlerident)
            REFERENCES innstillinger.innstillinger (saksbehandlerident)
);

CREATE INDEX innstilling_hjemmel_saksbehandleridentx ON innstillinger.innstillinger_hjemmel (innstillinger_saksbehandlerident);
CREATE INDEX innstilling_ytelse_saksbehandleridentx ON innstillinger.innstillinger_ytelse (innstillinger_saksbehandlerident);