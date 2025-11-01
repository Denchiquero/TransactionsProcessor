package com.example.paymentservice.service;

import com.example.paymentservice.client.GatewayClient;
import com.example.paymentservice.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.paymentservice.model.*;

import java.util.List;
import java.util.Optional;


@Service

public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentProcessor paymentProcessor;

    @Autowired
    private GatewayClient gatewayClient;

    @Autowired
    private PaymentCallbackService paymentCallbackService;

    @Transactional
    public Payment createPayment(PaymentRequest paymentRequest) {

        try {
            log.info("CREATE PAYMENT for order: {}, CardToken: {}",
                    paymentRequest.getOrderId(), paymentRequest.getCardToken());

            Payment payment = new Payment();
            payment.setStatus(PaymentStatus.PENDING);
            payment.setOrderId(paymentRequest.getOrderId());
            payment.setAmount(paymentRequest.getAmount());
            payment.setCurrency(paymentRequest.getCurrency() != null ? paymentRequest.getCurrency() : "RUB");
            payment.setCustomerEmail(paymentRequest.getCustomerEmail() != null ? paymentRequest.getCustomerEmail() : "unknown@example.com");
            payment.setDescription(paymentRequest.getDescription());
            payment.setCardToken(paymentRequest.getCardToken());
            payment.setStatus(PaymentStatus.PENDING);

            Payment savedPayment = paymentRepository.save(payment);
            log.info("PAYMENT SAVED - ID: {}",
                    savedPayment.getPaymentId());
            // Запускаем асинхронную обработку платежа
            paymentProcessor.processPaymentAsync(savedPayment.getId());

            log.info("ASYNC PAYMENT PROCESSING INITIATED FOR: {}", payment.getPaymentId());

            return payment;

        } catch (Exception e) {
            log.error("ERROR CREATING PAYMENT: {}", e.getMessage(), e);
            throw new RuntimeException("Payment creation failed: " + e.getMessage());
        }
    }


    @Transactional(readOnly = true)
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Payment> getPaymentByPaymentId(String paymentId) {
        return paymentRepository.findByPaymentId(paymentId);
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByCustomerEmail(String customerEmail) {
        return paymentRepository.findByCustomerEmail(customerEmail);
    }

    public Payment updatePaymentStatus(String paymentId, PaymentStatus status) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        payment.setStatus(status);
        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public long getTotalPaymentsCount() {
        return paymentRepository.count();
    }
}