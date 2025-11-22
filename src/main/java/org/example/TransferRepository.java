package org.example;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.example.jooq.Tables.TRANSFER;

@Repository
public class TransferRepository implements org.example.Repository<Transfer> {

    private final DSLContext dsl;

    public TransferRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Transfer save(Transfer transfer) {
        dsl.newRecord(TRANSFER, transfer).insert();
        return transfer;
    }

    @Override
    public void update(Transfer transfer) {
        throw new UnsupportedOperationException("Transfer entity does not support versioned updates");
    }

    public List<Transfer> findAll() {
        return dsl.selectFrom(TRANSFER).fetchInto(Transfer.class);
    }
}

