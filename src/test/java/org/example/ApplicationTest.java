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
        // Given
        long randomBalance = new Random().nextLong(1000L, 1000000L);
        Map<String, Object> request = Map.of(
            "balance", randomBalance,
            "currency", "USD"
        );

        // When - Create account
        ResponseEntity<Account> createResponse = restTemplate.postForEntity(
            "/accounts",
            request,
            Account.class
        );

        // Then - Verify creation was successful
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        assertNotNull(createResponse.getBody().id());

        // When - Fetch all accounts
        ResponseEntity<List<Account>> listResponse = restTemplate.exchange(
            "/accounts",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Account>>() {
            }
        );

        // Then - Verify the created account exists in the list
        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        assertNotNull(listResponse.getBody());

        Account createdAccount = createResponse.getBody();
        boolean accountFound = listResponse.getBody().stream()
            .anyMatch(account ->
                account.id().equals(createdAccount.id()) &&
                    account.balance() == randomBalance &&
                    account.currency().equals("USD")
            );

        assertTrue(accountFound, "Created account should exist in the list of all accounts");
    }

    @Test
    void createTransfer_shouldUpdateAccountBalancesAndCreateTransferRecord() {
        // Given - Create 2 accounts with predefined balances
        long account1InitialBalance = 1000L;
        long account2InitialBalance = 500L;

        Map<String, Object> account1Request = Map.of(
            "balance", account1InitialBalance,
            "currency", "USD"
        );
        Map<String, Object> account2Request = Map.of(
            "balance", account2InitialBalance,
            "currency", "USD"
        );

        ResponseEntity<Account> account1Response = restTemplate.postForEntity(
            "/accounts",
            account1Request,
            Account.class
        );
        ResponseEntity<Account> account2Response = restTemplate.postForEntity(
            "/accounts",
            account2Request,
            Account.class
        );

        assertEquals(HttpStatus.CREATED, account1Response.getStatusCode());
        assertEquals(HttpStatus.CREATED, account2Response.getStatusCode());
        assertNotNull(account1Response.getBody());
        assertNotNull(account2Response.getBody());

        UUID account1Id = account1Response.getBody().id();
        UUID account2Id = account2Response.getBody().id();

        // Calculate transfer amount (less than minimum balance to keep both accounts above zero)
        long transferAmount = Math.min(account1InitialBalance, account2InitialBalance) - 100L; // 400L

        // When - Create transfer from account1 to account2
        Map<String, Object> transferRequest = Map.of(
            "fromAccountId", account1Id.toString(),
            "toAccountId", account2Id.toString(),
            "amount", transferAmount
        );

        ResponseEntity<Transfer> transferResponse = restTemplate.postForEntity(
            "/transfers",
            transferRequest,
            Transfer.class
        );

        // Then - Verify transfer was created successfully
        assertEquals(HttpStatus.CREATED, transferResponse.getStatusCode());
        assertNotNull(transferResponse.getBody());

        // Fetch all accounts and verify balances
        ResponseEntity<List<Account>> accountsResponse = restTemplate.exchange(
            "/accounts",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Account>>() {
            }
        );

        assertEquals(HttpStatus.OK, accountsResponse.getStatusCode());
        assertNotNull(accountsResponse.getBody());

        List<Account> accounts = accountsResponse.getBody();
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
        ResponseEntity<List<Transfer>> transfersResponse = restTemplate.exchange(
            "/transfers",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Transfer>>() {
            }
        );

        assertEquals(HttpStatus.OK, transfersResponse.getStatusCode());
        assertNotNull(transfersResponse.getBody());

        List<Transfer> transfers = transfersResponse.getBody();

        // Find the transfer we just created
        Transfer createdTransfer = transfers.stream()
            .filter(t -> t.fromAccountId().equals(account1Id) &&
                t.toAccountId().equals(account2Id) &&
                t.amount() == transferAmount)
            .findFirst()
            .orElseThrow(() -> new AssertionError("Transfer record not found"));

        // Verify transfer details
        assertNotNull(createdTransfer.id(), "Transfer should have an ID");
        assertEquals(account1Id, createdTransfer.fromAccountId(), "Transfer fromAccountId should match");
        assertEquals(account2Id, createdTransfer.toAccountId(), "Transfer toAccountId should match");
        assertEquals(transferAmount, createdTransfer.amount(), "Transfer amount should match");
    }

}

