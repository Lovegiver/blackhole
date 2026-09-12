ALTER TABLE blackhole.content_registry
    ADD COLUMN idempotency_key UUID;

UPDATE blackhole.content_registry
SET idempotency_key = id
WHERE idempotency_key IS NULL;

ALTER TABLE blackhole.content_registry
    ALTER COLUMN idempotency_key SET NOT NULL,
    ADD CONSTRAINT content_registry_idempotency_key_unique UNIQUE (idempotency_key);
