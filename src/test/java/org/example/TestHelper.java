package org.example;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TestHelper {

    private final TestRestTemplate restTemplate;

    public TestHelper(TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Account createAccount(long balance, String currency) {
        Map<String, Object> request = Map.of("balance", balance, "currency", currency);
        var response = restTemplate.postForEntity("/accounts", request, Account.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        return response.getBody();
    }

    public Transfer createTransfer(UUID fromAccountId, UUID toAccountId, long amount) {
        Map<String, Object> request = Map.of(
            "fromAccountId", fromAccountId.toString(),
            "toAccountId", toAccountId.toString(),
            "amount", amount
        );

        var response = restTemplate.postForEntity("/transfers", request, Transfer.class);

        if (response.getStatusCode() != HttpStatus.CREATED) {
            throw new RuntimeException("Failed to create transfer: " + response.getStatusCode());
        }

        assertNotNull(response.getBody());
        return response.getBody();
    }

    public List<Account> getAllAccounts() {
        var response = restTemplate.exchange(
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

    public List<Transfer> getAllTransfers() {
        var response = restTemplate.exchange(
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

