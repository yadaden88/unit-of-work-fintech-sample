CREATE TABLE transfer (
    id UUID PRIMARY KEY NOT NULL,
    from_account_id UUID NOT NULL,
    to_account_id UUID NOT NULL,
    amount BIGINT NOT NULL,
    CONSTRAINT fk_transfer_from_account FOREIGN KEY (from_account_id) REFERENCES account(id),
    CONSTRAINT fk_transfer_to_account FOREIGN KEY (to_account_id) REFERENCES account(id)
);

