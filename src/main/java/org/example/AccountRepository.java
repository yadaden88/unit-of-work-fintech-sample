package org.example;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.example.jooq.Tables.ACCOUNT;

@Repository
public class AccountRepository {

    private final DSLContext dsl;

    public AccountRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Account save(Account account) {
        dsl.newRecord(ACCOUNT, account).insert();
        return account;
    }

    public List<Account> findAll() {
        return dsl.selectFrom(ACCOUNT).fetchInto(Account.class);
    }
}

