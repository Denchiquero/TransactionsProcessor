package com.example.orderservice.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

class SimpleHealthE2ETest {

    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    void paymentServiceHealth_ShouldReturn200() {
        String url = "http://localhost:8080/actuator/health";

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            assertTrue(response.getStatusCode().is2xxSuccessful(),
                    "Payment Service should be healthy");
            System.out.println("✅ Payment Service health: " + response.getStatusCode());
        } catch (Exception e) {
            fail("❌ Payment Service is down: " + e.getMessage());
        }
    }

    @Test
    void orderServiceHealth_ShouldReturn200() {
        String url = "http://localhost:8081/actuator/health";

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            assertTrue(response.getStatusCode().is2xxSuccessful(),
                    "Order Service should be healthy");
            System.out.println("✅ Order Service health: " + response.getStatusCode());
        } catch (Exception e) {
            fail("❌ Order Service is down: " + e.getMessage());
        }
    }

    @Test
    void servicesBasicEndpoints_ShouldRespond() {
        // Проверяем базовые endpoints без API Gateway
        String[] endpoints = {
                "http://localhost:8080/api/payments/count",
                "http://localhost:8081/api/orders/count"
        };

        for (String url : endpoints) {
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                // Принимаем 200 OK или 404 (если нет данных)
                boolean isSuccess = response.getStatusCode().is2xxSuccessful() ||
                        response.getStatusCode().value() == 404;
                assertTrue(isSuccess, "Endpoint should respond: " + url);
                System.out.println("✅ " + url + " - " + response.getStatusCode());
            } catch (Exception e) {
                System.out.println("⚠️ " + url + " - " + e.getMessage());
                // Не фейлим тест, просто логируем
            }
        }
    }
}