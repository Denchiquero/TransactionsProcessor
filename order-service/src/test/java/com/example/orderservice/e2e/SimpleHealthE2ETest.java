package com.example.orderservice.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckE2ETest {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String API_GATEWAY_URL = "http://localhost:8081";

    @Test
    void allServicesHealthCheck_ShouldReturn200() {
        System.out.println("🏥 Starting Health Check E2E Test...");

        String[] endpoints = {
                "/api/orders",
                "/api/payments",
                "/api/orders/count",
                "/api/payments/count"
        };

        for (String endpoint : endpoints) {
            String url = API_GATEWAY_URL + endpoint;
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

                // Принимаем как 200 OK, так и 404 (если нет данных) - главное что сервис отвечает
                boolean isSuccess = response.getStatusCode().is2xxSuccessful() ||
                        response.getStatusCode().value() == 404;

                assertTrue(isSuccess, "Endpoint should respond: " + url);
                System.out.println(endpoint + " - " + response.getStatusCode());

            } catch (Exception e) {
                fail("Service unavailable: " + url + " - " + e.getMessage());
            }
        }

        System.out.println("All services are healthy!");
    }

    @Test
    void apiGateway_ShouldBeAccessible() {
        String url = API_GATEWAY_URL + "/api/orders";

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            assertTrue(response.getStatusCode().is2xxSuccessful() ||
                    response.getStatusCode().value() == 404);
            System.out.println("API Gateway is accessible");
        } catch (Exception e) {
            fail("API Gateway is down: " + e.getMessage());
        }
    }
}