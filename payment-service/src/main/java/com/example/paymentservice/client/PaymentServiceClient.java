//package com.example.paymentservice.client;
//
//import com.example.paymentservice.model.PaymentRequest;
//import com.example.paymentservice.model.PaymentResponse;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//
//@FeignClient(name = "payment-service", url = "payment-service:8080")
//public interface PaymentServiceClient {
//
//    @PostMapping("/api/payments")
//    PaymentResponse createPayment(@RequestBody PaymentRequest paymentRequest);
//
//    @GetMapping("/api/payments/payment/{paymentId}")
//    PaymentResponse getPaymentByPaymentId(@PathVariable String paymentId);
//
//    @GetMapping("/api/payments/order/{orderId}")
//    PaymentResponse getPaymentsByOrderId(@PathVariable String orderId);
//
//    @PostMapping("/api/payments/sync")
//    PaymentResponse createPaymentSync(@RequestBody PaymentRequest paymentRequest);
//}
