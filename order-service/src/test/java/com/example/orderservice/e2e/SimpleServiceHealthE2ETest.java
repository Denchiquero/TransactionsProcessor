package com.example.orderservice.e2e;

import org.junit.jupiter.api.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

class SimpleServiceHealthE2ETest {

    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    void bothServices_ShouldBeHealthy() {
        System.out.println("🚀 Starting E2E health check...");

        // Проверяем Payment Service
        assertDoesNotThrow(() -> {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    "http://localhost:8080/actuator/health", String.class);
            assertTrue(response.getStatusCode().is2xxSuccessful());
            System.out.println("✅ Payment Service is healthy");
        }, "Payment Service should be healthy");

        // Проверяем Order Service
        assertDoesNotThrow(() -> {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    "http://localhost:8081/actuator/health", String.class);
            assertTrue(response.getStatusCode().is2xxSuccessful());
            System.out.println("✅ Order Service is healthy");
        }, "Order Service should be healthy");

        System.out.println("🎉 Both services are healthy!");
    }
}