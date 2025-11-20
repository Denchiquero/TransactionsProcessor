package com.example.orderservice.integration;

import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class OrderServiceIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:14")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void createOrder_ShouldSaveToDatabase() {
        // Given
        Order order = new Order();
        order.setCustomerEmail("integration@test.com");
        order.setCustomerName("Integration Test");
        order.setShippingAddress("Test Address");
        order.setStatus(OrderStatus.PENDING);
        order.setCardToken("test_token_123");

        // When
        Order savedOrder = orderRepository.save(order);

        // Then
        assertNotNull(savedOrder.getId());
        assertNotNull(savedOrder.getOrderId());
        assertEquals("integration@test.com", savedOrder.getCustomerEmail());
        assertEquals(OrderStatus.PENDING, savedOrder.getStatus());

        // Verify it can be retrieved from database
        Optional<Order> foundOrder = orderRepository.findByOrderId(savedOrder.getOrderId());
        assertTrue(foundOrder.isPresent());
        assertEquals(savedOrder.getId(), foundOrder.get().getId());
    }

    @Test
    void updateOrderStatus_ShouldPersistChanges() {
        // Given
        Order order = new Order();
        order.setCustomerEmail("update@test.com");
        order.setCustomerName("Update Test");
        order.setShippingAddress("Update Address");
        order.setStatus(OrderStatus.PENDING);
        order.setCardToken("update_token_123");
        Order savedOrder = orderRepository.save(order);

        // When
        Order updatedOrder = orderService.updateOrderStatus(
                savedOrder.getOrderId(),
                OrderStatus.PAYMENT_COMPLETED
        );

        // Then
        assertEquals(OrderStatus.PAYMENT_COMPLETED, updatedOrder.getStatus());

        // Verify change is persisted
        Optional<Order> foundOrder = orderRepository.findByOrderId(savedOrder.getOrderId());
        assertTrue(foundOrder.isPresent());
        assertEquals(OrderStatus.PAYMENT_COMPLETED, foundOrder.get().getStatus());
    }
}