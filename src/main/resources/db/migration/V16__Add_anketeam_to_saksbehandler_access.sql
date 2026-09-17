ALTER TABLE innstillinger.saksbehandler_access
    ADD COLUMN IF NOT EXISTS anketeam BOOLEAN NOT NULL DEFAULT FALSE;
