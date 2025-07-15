CREATE
EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE product
(
    id         UUID PRIMARY KEY      DEFAULT uuid_generate_v4(),
    name       VARCHAR(100) NOT NULL,
    quantity   NUMERIC      NOT NULL,
    price      NUMERIC      NOT NULL,
    voided     BOOLEAN      NOT NULL DEFAULT false,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX unique_non_voided_product_name ON product (LOWER(name)) WHERE voided = false;

-- Index searchable columns
CREATE INDEX idx_product_name ON product (name);