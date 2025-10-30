// [file name]: OrderController.java
package com.example.orderservice.controller;

import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderRequest;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.model.PaymentCallbackRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        try {
            Order order = orderService.createOrder(orderRequest);
            return ResponseEntity.accepted().body(order);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("Internal server error"));
        }
    }

    @PostMapping("/{orderId}/payment-callback")
    public ResponseEntity<?> paymentCallback(
            @PathVariable String orderId,
            @RequestBody PaymentCallbackRequest callbackRequest) {

        orderService.processPaymentCallback(
                orderId,
                callbackRequest.getStatus(),
                callbackRequest.getPaymentId(),
                callbackRequest.getErrorMessage()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/retry-payment")
    public ResponseEntity<?> retryPayment(@PathVariable String orderId) {
        boolean success = orderService.retryPayment(orderId);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().body(createErrorResponse("Не удалось повторить платеж"));
        }
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Optional<Order> order = orderService.getOrderById(id);
        return order.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Order> getOrderByOrderId(@PathVariable String orderId) {
        Optional<Order> order = orderService.getOrderByOrderId(orderId);
        return order.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{email}")
    public ResponseEntity<List<Order>> getOrdersByCustomerEmail(@PathVariable String email) {
        List<Order> orders = orderService.getOrdersByCustomerEmail(email);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable OrderStatus status) {
        List<Order> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable String orderId,
            @RequestParam OrderStatus status) {
        try {
            Order order = orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getTotalOrdersCount() {
        long count = orderService.getTotalOrdersCount();
        return ResponseEntity.ok(count);
    }

    private ErrorResponse createErrorResponse(String message) {
        return new ErrorResponse("ERROR", message);
    }

    // Внутренний класс для ошибок
    private static class ErrorResponse {
        private String status;
        private String error;

        public ErrorResponse(String status, String error) {
            this.status = status;
            this.error = error;
        }

        public String getStatus() { return status; }
        public String getError() { return error; }
    }
}