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
        dsl.insertInto(ACCOUNT)
            .columns(
                ACCOUNT.ID,
                ACCOUNT.BALANCE,
                ACCOUNT.CURRENCY
            )
            .values(
                account.id(),
                account.balance(),
                account.currency()
            )
            .execute();

        return account;
    }

    public List<Account> findAll() {
        return dsl.select(
                ACCOUNT.ID,
                ACCOUNT.BALANCE,
                ACCOUNT.CURRENCY
            )
            .from(ACCOUNT)
            .fetch(record -> new Account(
                record.value1(),
                record.value2(),
                record.value3()
            ));
    }
}

