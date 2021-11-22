DO
$$
    BEGIN
        IF EXISTS
            (SELECT 1 from pg_roles where rolname = 'cloudsqliamuser')
        THEN
            GRANT USAGE ON SCHEMA public TO cloudsqliamuser;
            GRANT USAGE ON SCHEMA innstillinger TO cloudsqliamuser;
            GRANT SELECT ON ALL TABLES IN SCHEMA public TO cloudsqliamuser;
            GRANT SELECT ON ALL TABLES IN SCHEMA innstillinger TO cloudsqliamuser;
            ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO cloudsqliamuser;
            ALTER DEFAULT PRIVILEGES IN SCHEMA innstillinger GRANT SELECT ON TABLES TO cloudsqliamuser;
        END IF;
    END
$$;


CREATE TABLE innstillinger.valgt_enhet
(
    saksbehandlerident TEXT PRIMARY KEY,
    enhet_id           TEXT      NOT NULL,
    enhet_navn         TEXT      NOT NULL,
    tidspunkt          TIMESTAMP NOT NULL
);

CREATE TABLE innstillinger.innstillinger
(
    saksbehandlerident TEXT PRIMARY KEY,
    hjemler            TEXT      NOT NULL,
    ytelser             TEXT      NOT NULL,
    typer              TEXT      NOT NULL,
    tidspunkt          TIMESTAMP NOT NULL
);