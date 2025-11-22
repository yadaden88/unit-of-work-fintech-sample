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

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class AccountControllerTest {

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
                new ParameterizedTypeReference<List<Account>>() {}
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

}

