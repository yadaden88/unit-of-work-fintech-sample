package org.example;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.example.jooq.Tables.TRANSFER;

@Repository
public class TransferRepository {

    private final DSLContext dsl;

    public TransferRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Transfer save(Transfer transfer) {
        dsl.newRecord(TRANSFER, transfer).insert();
        return transfer;
    }

    public List<Transfer> findAll() {
        return dsl.selectFrom(TRANSFER).fetchInto(Transfer.class);
    }
}

