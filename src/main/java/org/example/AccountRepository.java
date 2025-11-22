package org.example;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@Repository
public class AccountRepository {
    
    private final DSLContext dsl;
    
    public AccountRepository(DSLContext dsl) {
        this.dsl = dsl;
    }
    
    public Account save(Account account) {
        dsl.insertInto(table("account"))
            .columns(
                field("id"),
                field("balance"),
                field("currency")
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
                field("id", UUID.class),
                field("balance", Long.class),
                field("currency", String.class)
            )
            .from(table("account"))
            .fetch(record -> new Account(
                record.value1(),
                record.value2(),
                record.value3()
            ));
    }
}

