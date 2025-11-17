// [file name]: OrderController.java
package com.example.orderservice.controller;

import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderRequest;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.model.PaymentCallbackRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        log.info("Received order request with card token: {}", orderRequest.getCardToken());
        try {
            Order order = orderService.createOrder(orderRequest);
            orderService.notifyReportServiceAsync(order.getOrderId());
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
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
        log.info("Searching for order: {}", orderId);
        Optional<Order> order = orderService.getOrderByOrderId(orderId);
        return order.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

//    @GetMapping("/order/{orderId}")
//    public ResponseEntity<Order> getOrderByOrderId(@PathVariable String orderId) {
//        log.info("🔍 [REPORT-SERVICE CALL] Searching for order: {}", orderId);
//
//        // Логируем ВСЕ заказы в БД для дебага
//        List<Order> allOrders = orderService.getAllOrders();
//        log.info("📋 All orders in database ({} total):", allOrders.size());
//        allOrders.forEach(order ->
//                log.info("   - {}: status={}, created={}",
//                        order.getOrderId(), order.getStatus(), order.getCreatedAt())
//        );
//
//        Optional<Order> order = orderService.getOrderByOrderId(orderId);
//
//        if (order.isPresent()) {
//            log.info("Order FOUND: {}", orderId);
//            return ResponseEntity.ok(order.get());
//        } else {
//            log.error("Order NOT FOUND: {}", orderId);
//
//            // Логируем ближайшие заказы по времени
//            List<Order> recentOrders = allOrders.stream()
//                    .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
//                    .limit(5)
//                    .collect(Collectors.toList());
//            log.info("Recent orders:");
//            recentOrders.forEach(recent ->
//                    log.info("   - {}: status={}, created={}",
//                            recent.getOrderId(), recent.getStatus(), recent.getCreatedAt())
//            );
//
//            return ResponseEntity.notFound().build();
//        }
//    }

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