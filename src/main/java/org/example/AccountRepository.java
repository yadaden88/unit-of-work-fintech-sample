package org.example;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static org.example.jooq.Tables.ACCOUNT;

@Repository
public class AccountRepository implements org.example.Repository<Account> {

    private final DSLContext dsl;

    public AccountRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Account save(Account account) {
        dsl.newRecord(ACCOUNT, account).insert();
        return account;
    }

    @Override
    public void update(Account account) {
        int rowsUpdated = dsl.update(ACCOUNT)
            .set(ACCOUNT.BALANCE, account.balance())
            .set(ACCOUNT.CURRENCY, account.currency())
            .set(ACCOUNT.VERSION, account.version() + 1)
            .where(ACCOUNT.ID.eq(account.id()))
            .and(ACCOUNT.VERSION.eq(account.version()))
            .execute();

        if (rowsUpdated == 0) {
            throw new OptimisticLockException(
                "Account with id " + account.id() + " has been modified by another transaction. " +
                    "Expected version: " + account.version()
            );
        }
    }

    public List<Account> findAll() {
        return dsl.selectFrom(ACCOUNT).fetchInto(Account.class);
    }

    public Account findById(UUID id) {
        return dsl.selectFrom(ACCOUNT)
            .where(ACCOUNT.ID.eq(id))
            .fetchOneInto(Account.class);
    }
}

