package com.example.orderservice.integration;

import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class OrderControllerIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:14")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void getAllOrders_ShouldReturnOrdersFromDatabase() throws Exception {
        // Given - сохраняем заказ в базу
        Order order = new Order();
        order.setCustomerEmail("api@test.com");
        order.setCustomerName("API Test");
        order.setShippingAddress("API Address");
        order.setStatus(OrderStatus.PENDING);
        order.setCardToken("api_token_123");
        orderRepository.save(order);

        // When & Then - проверяем API endpoint
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].customerEmail").value("api@test.com"));
    }

    @Test
    void getOrderByOrderId_ShouldReturnOrderFromDatabase() throws Exception {
        // Given
        Order order = new Order();
        order.setCustomerEmail("getbyid@test.com");
        order.setCustomerName("GetById Test");
        order.setShippingAddress("GetById Address");
        order.setStatus(OrderStatus.PAYMENT_COMPLETED);
        order.setCardToken("getbyid_token_123");
        Order savedOrder = orderRepository.save(order);

        // When & Then
        mockMvc.perform(get("/api/orders/order/{orderId}", savedOrder.getOrderId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerEmail").value("getbyid@test.com"))
                .andExpect(jsonPath("$.status").value("PAYMENT_COMPLETED"));
    }

    @Test
    void updateOrderStatus_ShouldUpdateInDatabase() throws Exception {
        // Given
        Order order = new Order();
        order.setCustomerEmail("update@test.com");
        order.setCustomerName("Update Test");
        order.setShippingAddress("Update Address");
        order.setStatus(OrderStatus.PENDING);
        order.setCardToken("update_token_123");
        Order savedOrder = orderRepository.save(order);

        // When & Then
        mockMvc.perform(put("/api/orders/{orderId}/status", savedOrder.getOrderId())
                        .param("status", "PAYMENT_COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAYMENT_COMPLETED"));

        // Verify in database
        Order updatedOrder = orderRepository.findByOrderId(savedOrder.getOrderId()).orElseThrow();
        assertEquals(OrderStatus.PAYMENT_COMPLETED, updatedOrder.getStatus());
    }
}