package com.example.orderservice.units;

import com.example.orderservice.controller.OrderController;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    void getAllOrders_ShouldReturnOrders() throws Exception {
        // Given
        Order order1 = new Order();
        Order order2 = new Order();
        when(orderService.getAllOrders()).thenReturn(Arrays.asList(order1, order2));

        // When & Then
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getOrderById_ShouldReturnOrder_WhenExists() throws Exception {
        // Given
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        when(orderService.getOrderById(orderId)).thenReturn(Optional.of(order));

        // When & Then
        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk());
    }

    @Test
    void getOrderById_ShouldReturnNotFound_WhenNotExists() throws Exception {
        // Given
        Long orderId = 999L;
        when(orderService.getOrderById(orderId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrderByOrderId_ShouldReturnOrder() throws Exception {
        // Given
        String orderId = "ORD_123";
        Order order = new Order();
        order.setOrderId(orderId);
        when(orderService.getOrderByOrderId(orderId)).thenReturn(Optional.of(order));

        // When & Then
        mockMvc.perform(get("/api/orders/order/{orderId}", orderId))
                .andExpect(status().isOk());
    }

    @Test
    void updateOrderStatus_ShouldUpdateSuccessfully() throws Exception {
        // Given
        String orderId = "ORD_123";
        Order order = new Order();
        order.setOrderId(orderId);
        when(orderService.updateOrderStatus(any(String.class), any(OrderStatus.class))).thenReturn(order);

        // When & Then
        mockMvc.perform(put("/api/orders/{orderId}/status", orderId)
                        .param("status", "PAYMENT_COMPLETED"))
                .andExpect(status().isOk());
    }
}