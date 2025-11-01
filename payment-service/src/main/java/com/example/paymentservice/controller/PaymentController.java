package com.example.paymentservice.controller;

import com.example.paymentservice.model.Payment;
import com.example.paymentservice.model.PaymentRequest;
import com.example.paymentservice.model.PaymentResponse;
import com.example.paymentservice.model.PaymentStatus;
import com.example.paymentservice.service.PaymentProcessor;
import com.example.paymentservice.service.PaymentService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private PaymentProcessor paymentProcessor;

    @PostMapping
    public ResponseEntity<?> createPayment(@RequestBody PaymentRequest paymentRequest) {
        try {
            log.info("Received payment request for order: {}", paymentRequest.getOrderId());

            // Базовая валидация
            if (paymentRequest.getAmount() == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("Сумма платежа обязательна"));
            }
            if (paymentRequest.getAmount().doubleValue() <= 0) {
                return ResponseEntity.badRequest().body(createErrorResponse("Сумма должна быть больше 0"));
            }
            if (paymentRequest.getOrderId() == null || paymentRequest.getOrderId().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Order ID обязателен"));
            }

            // Используем синхронную обработку
            PaymentResponse paymentResponse = paymentProcessor.processPaymentImmediately(paymentRequest);
            return ResponseEntity.ok(paymentResponse);

        } catch (IllegalArgumentException e) {
            log.warn("Validation error in payment request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Internal server error in createPayment: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("Внутренняя ошибка сервера: " + e.getMessage()));
        }
    }

    // Добавляем синхронный endpoint (можно переименовать основной)
    @PostMapping("/sync")
    public ResponseEntity<?> createPaymentSync(@RequestBody PaymentRequest paymentRequest) {
        return createPayment(paymentRequest); // делегируем основному методу
    }

    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        List<Payment> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        Optional<Payment> payment = paymentService.getPaymentById(id);
        return payment.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<Payment> getPaymentByPaymentId(@PathVariable String paymentId) {
        Optional<Payment> payment = paymentService.getPaymentByPaymentId(paymentId);
        return payment.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Payment>> getPaymentsByOrderId(@PathVariable String orderId) {
        List<Payment> payments = paymentService.getPaymentsByOrderId(orderId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Payment>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        List<Payment> payments = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/customer/{email}")
    public ResponseEntity<List<Payment>> getPaymentsByCustomerEmail(@PathVariable String email) {
        List<Payment> payments = paymentService.getPaymentsByCustomerEmail(email);
        return ResponseEntity.ok(payments);
    }

    @PutMapping("/{paymentId}/status")
    public ResponseEntity<Payment> updatePaymentStatus(
            @PathVariable String paymentId,
            @RequestParam PaymentStatus status) {
        try {
            Payment payment = paymentService.updatePaymentStatus(paymentId, status);
            return ResponseEntity.ok(payment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getTotalPaymentsCount() {
        long count = paymentService.getTotalPaymentsCount();
        return ResponseEntity.ok(count);
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        response.put("status", "ERROR");
        return response;
    }
}