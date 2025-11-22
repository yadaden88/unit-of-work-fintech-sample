CREATE TABLE account (
    id UUID PRIMARY KEY,
    balance BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    CONSTRAINT currency_check CHECK (currency IN ('USD', 'GBP', 'EUR'))
);

