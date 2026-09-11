CREATE SCHEMA IF NOT EXISTS blackhole AUTHORIZATION CURRENT_USER;

REVOKE ALL ON SCHEMA blackhole FROM PUBLIC;

CREATE TABLE blackhole.content_registry (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content_hash CHAR(64) NOT NULL,
    registered_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    CONSTRAINT content_registry_content_hash_unique UNIQUE (content_hash),
    CONSTRAINT content_registry_content_hash_format
        CHECK (content_hash ~ '^[0-9a-f]{64}$')
);

REVOKE ALL ON TABLE blackhole.content_registry FROM PUBLIC;
GRANT USAGE ON SCHEMA blackhole TO ${runtimeRole};
GRANT SELECT, INSERT ON TABLE blackhole.content_registry TO ${runtimeRole};
