CREATE TABLE account (
    id UUID PRIMARY KEY NOT NULL,
    balance BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL
);

