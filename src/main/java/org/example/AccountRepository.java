package org.example;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

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

    public Account findById(UUID id) {
        return dsl.selectFrom(ACCOUNT)
            .where(ACCOUNT.ID.eq(id))
            .fetchOneInto(Account.class);
    }

    public void update(Account account) {
        dsl.update(ACCOUNT)
            .set(ACCOUNT.BALANCE, account.balance())
            .set(ACCOUNT.CURRENCY, account.currency())
            .where(ACCOUNT.ID.eq(account.id()))
            .execute();
    }
}

