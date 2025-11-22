package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class ApplicationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private TestHelper testHelper;

    @Test
    void createAccount_and_listAccounts_shouldReturnCreatedAccountWithGeneratedId() {
        testHelper = new TestHelper(restTemplate);
        var randomBalance = new Random().nextLong(1000L, 1000000L);

        // When - Create account
        var createdAccount = testHelper.createAccount(randomBalance);

        // Then - Verify creation was successful
        assertNotNull(createdAccount.id());

        // When - Fetch all accounts
        var accounts = testHelper.getAllAccounts();

        // Then - Verify the created account exists in the list
        var accountFound = accounts.stream()
            .anyMatch(account ->
                account.id().equals(createdAccount.id()) &&
                    account.balance() == randomBalance
            );

        assertTrue(accountFound, "Created account should exist in the list of all accounts");
    }

    @Test
    void createTransfer_shouldUpdateAccountBalancesAndCreateTransferRecord() {
        testHelper = new TestHelper(restTemplate);

        // Given - Create 2 accounts with random balances
        var random = new Random();
        var account1InitialBalance = random.nextLong(1000L, 10000L);
        var account2InitialBalance = random.nextLong(1000L, 10000L);

        var account1 = testHelper.createAccount(account1InitialBalance);
        var account2 = testHelper.createAccount(account2InitialBalance);

        var account1Id = account1.id();
        var account2Id = account2.id();

        var transferAmount = random.nextLong(100L, 900L);

        // When - Create transfer from account1 to account2
        testHelper.createTransfer(account1Id, account2Id, transferAmount);

        // Then - Fetch all accounts and verify balances
        var accounts = testHelper.getAllAccounts();

        var updatedAccount1 = accounts.stream()
            .filter(acc -> acc.id().equals(account1Id))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Account 1 not found"));

        var updatedAccount2 = accounts.stream()
            .filter(acc -> acc.id().equals(account2Id))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Account 2 not found"));

        // Verify account1 balance decreased by transfer amount
        assertEquals(account1InitialBalance - transferAmount, updatedAccount1.balance(),
            "Account 1 balance should be decreased by transfer amount");

        // Verify account2 balance increased by transfer amount
        assertEquals(account2InitialBalance + transferAmount, updatedAccount2.balance(),
            "Account 2 balance should be increased by transfer amount");

        // Fetch all transfers and verify the transfer record
        var transfers = testHelper.getAllTransfers();

        // Find the transfer we just created
        var foundTransfer = transfers.stream()
            .filter(t -> t.fromAccountId().equals(account1Id) &&
                t.toAccountId().equals(account2Id) &&
                t.amount() == transferAmount)
            .findFirst()
            .orElseThrow(() -> new AssertionError("Transfer record not found"));

        // Verify transfer details
        assertNotNull(foundTransfer.id(), "Transfer should have an ID");
        assertEquals(account1Id, foundTransfer.fromAccountId(), "Transfer fromAccountId should match");
        assertEquals(account2Id, foundTransfer.toAccountId(), "Transfer toAccountId should match");
        assertEquals(transferAmount, foundTransfer.amount(), "Transfer amount should match");
    }
}

