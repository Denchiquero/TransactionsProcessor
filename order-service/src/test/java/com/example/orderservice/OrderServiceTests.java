package com.example.orderservice;

import com.example.orderservice.client.PaymentServiceClient;
import com.example.orderservice.client.ReportServiceClient;
import com.example.orderservice.model.*;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTests {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentServiceClient paymentServiceClient;

    @Mock
    private ReportServiceClient reportServiceClient;

    @InjectMocks
    private OrderService orderService;

    @Test
    void getAllOrders_ShouldReturnAllOrders() {
        // Given
        Order order1 = new Order();
        Order order2 = new Order();
        when(orderRepository.findAll()).thenReturn(Arrays.asList(order1, order2));

        // When
        List<Order> orders = orderService.getAllOrders();

        // Then
        assertNotNull(orders);
        assertEquals(2, orders.size());
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void getOrderById_ShouldReturnOrder_WhenExists() {
        // Given
        Long orderId = 1L;
        Order order = new Order();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // When
        Optional<Order> result = orderService.getOrderById(orderId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(order, result.get());
    }

    @Test
    void getOrderById_ShouldReturnEmpty_WhenNotExists() {
        // Given
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When
        Optional<Order> result = orderService.getOrderById(orderId);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void getTotalOrdersCount_ShouldReturnCount() {
        // Given
        when(orderRepository.count()).thenReturn(10L);

        // When
        long count = orderService.getTotalOrdersCount();

        // Then
        assertEquals(10L, count);
        verify(orderRepository, times(1)).count();
    }

    @Test
    void updateOrderStatus_ShouldUpdateStatus() {
        // Given
        String orderId = "ORD_123";
        Order order = new Order();
        order.setOrderId(orderId);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // When
        Order updatedOrder = orderService.updateOrderStatus(orderId, OrderStatus.PAYMENT_COMPLETED);

        // Then
        assertNotNull(updatedOrder);
        assertEquals(OrderStatus.PAYMENT_COMPLETED, updatedOrder.getStatus());
        verify(orderRepository, times(1)).save(order);
    }
}