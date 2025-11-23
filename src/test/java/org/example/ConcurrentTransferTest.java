package org.example;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.example.jooq.Tables.ACCOUNT;
import static org.example.jooq.Tables.TRANSFER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class ConcurrentTransferTest {

    private static final int THREADS_NUM = 20;
    private static final int NUMBER_OF_ACCOUNTS = 100;
    private static final int NUMBER_OF_TRANSFERS = NUMBER_OF_ACCOUNTS * 100;
    private static final long MIN_TRANSFER_AMOUNT = 5L;
    private static final long MAX_TRANSFER_AMOUNT = 50L;
    private static final long SAFETY_BUFFER_MIN = 100L;
    private static final long SAFETY_BUFFER_MAX = 10_000L;
    private static final long MIN_ACCOUNT_BALANCE = MAX_TRANSFER_AMOUNT * NUMBER_OF_TRANSFERS + SAFETY_BUFFER_MIN;
    private static final long MAX_ACCOUNT_BALANCE = MAX_TRANSFER_AMOUNT * NUMBER_OF_TRANSFERS + SAFETY_BUFFER_MAX;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DSLContext dsl;

    private TestHelper testHelper;

    @BeforeEach
    void cleanDatabase() {
        dsl.truncate(TRANSFER).cascade().execute();
        dsl.truncate(ACCOUNT).cascade().execute();
    }

    @Test
    void concurrentTransfers_shouldMaintainBalanceConservation() {
        testHelper = new TestHelper(restTemplate);

        var accounts = createAccounts();
        var initialTotalBalance = calculateTotalBalance(accounts);

        waitForAllTransfersToComplete(executeConcurrentTransfers(accounts));

        assertEquals(NUMBER_OF_TRANSFERS, testHelper.getAllTransfers().size(), "All executed transfers should be present");
        assertEquals(initialTotalBalance, calculateTotalBalance(testHelper.getAllAccounts()), "Total balance should remain the same.");
    }

    private List<Account> createAccounts() {
        var accounts = new ArrayList<Account>();
        var random = new Random();

        for (int i = 0; i < NUMBER_OF_ACCOUNTS; i++) {
            accounts.add(testHelper.createAccount(random.nextLong(MIN_ACCOUNT_BALANCE, MAX_ACCOUNT_BALANCE + 1)));
        }

        return accounts;
    }

    private long calculateTotalBalance(List<Account> accounts) {
        return accounts.stream()
            .mapToLong(Account::balance)
            .sum();
    }

    private List<CompletableFuture<Void>> executeConcurrentTransfers(List<Account> accounts) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        var random = new Random();
        var executorService = newFixedThreadPool(THREADS_NUM);

        for (int i = 0; i < NUMBER_OF_TRANSFERS; i++) {
            CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
                var fromAccount = accounts.get(random.nextInt(accounts.size()));
                var toAccount = selectDifferentRandomAccount(accounts, fromAccount, random);
                testHelper.createTransfer(fromAccount.id(), toAccount.id(), random.nextLong(MIN_TRANSFER_AMOUNT, MAX_TRANSFER_AMOUNT));
                return null;
            }, executorService);

            futures.add(future);
        }

        executorService.shutdown();
        return futures;
    }

    private Account selectDifferentRandomAccount(List<Account> accounts, Account excludeAccount, Random random) {
        Account selectedAccount;
        do {
            selectedAccount = accounts.get(random.nextInt(accounts.size()));
        } while (selectedAccount.id().equals(excludeAccount.id()));
        return selectedAccount;
    }

    private void waitForAllTransfersToComplete(List<CompletableFuture<Void>> futures) {
        futures.forEach(future -> {
            try {
                future.get(1, TimeUnit.MINUTES);
            } catch (TimeoutException | InterruptedException | ExecutionException e) {
                fail("Transfers did not complete within the expected time");
            }
        });
    }
}

