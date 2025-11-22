package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class ApplicationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createAccount_and_listAccounts_shouldReturnCreatedAccountWithGeneratedId() {
        long randomBalance = new Random().nextLong(1000L, 1000000L);

        // When - Create account
        Account createdAccount = createAccount(randomBalance, "USD");

        // Then - Verify creation was successful
        assertNotNull(createdAccount.id());

        // When - Fetch all accounts
        List<Account> accounts = getAllAccounts();

        // Then - Verify the created account exists in the list
        boolean accountFound = accounts.stream()
            .anyMatch(account ->
                account.id().equals(createdAccount.id()) &&
                    account.balance() == randomBalance &&
                    account.currency().equals("USD")
            );

        assertTrue(accountFound, "Created account should exist in the list of all accounts");
    }

    @Test
    void createTransfer_shouldUpdateAccountBalancesAndCreateTransferRecord() {
        // Given - Create 2 accounts with random balances
        Random random = new Random();
        long account1InitialBalance = random.nextLong(1000L, 10000L);
        long account2InitialBalance = random.nextLong(1000L, 10000L);

        Account account1 = createAccount(account1InitialBalance, "USD");
        Account account2 = createAccount(account2InitialBalance, "USD");

        UUID account1Id = account1.id();
        UUID account2Id = account2.id();

        long transferAmount = random.nextLong(100L, 900L);

        // When - Create transfer from account1 to account2
        createTransfer(account1Id, account2Id, transferAmount);

        // Then - Fetch all accounts and verify balances
        List<Account> accounts = getAllAccounts();

        Account updatedAccount1 = accounts.stream()
            .filter(acc -> acc.id().equals(account1Id))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Account 1 not found"));

        Account updatedAccount2 = accounts.stream()
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
        List<Transfer> transfers = getAllTransfers();

        // Find the transfer we just created
        Transfer foundTransfer = transfers.stream()
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

    private Account createAccount(long balance, String currency) {
        Map<String, Object> request = Map.of("balance", balance, "currency", currency);
        ResponseEntity<Account> response = restTemplate.postForEntity("/accounts", request, Account.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        return response.getBody();
    }

    private List<Account> getAllAccounts() {
        ResponseEntity<List<Account>> response = restTemplate.exchange(
            "/accounts",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Account>>() {
            }
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        return response.getBody();
    }

    private Transfer createTransfer(UUID fromAccountId, UUID toAccountId, long amount) {
        Map<String, Object> request = Map.of(
            "fromAccountId", fromAccountId.toString(),
            "toAccountId", toAccountId.toString(),
            "amount", amount
        );

        ResponseEntity<Transfer> response = restTemplate.postForEntity("/transfers", request, Transfer.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        return response.getBody();
    }

    private List<Transfer> getAllTransfers() {
        ResponseEntity<List<Transfer>> response = restTemplate.exchange(
            "/transfers",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Transfer>>() {
            }
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        return response.getBody();
    }

}

