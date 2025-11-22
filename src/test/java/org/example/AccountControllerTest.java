package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class AccountControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createAccount_shouldReturnCreatedAccountWithGeneratedId() {
        // Given
        Map<String, Object> request = Map.of(
                "balance", 10000L,
                "currency", "USD"
        );

        // When
        ResponseEntity<Account> response = restTemplate.postForEntity(
                "/accounts",
                request,
                Account.class
        );

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().id());
        assertEquals(10000L, response.getBody().balance());
        assertEquals("USD", response.getBody().currency());
    }

}

