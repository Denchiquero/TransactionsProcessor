package com.example.paymentservice.service;

import com.example.paymentservice.client.GatewayClient;
import com.example.paymentservice.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.example.paymentservice.model.*;

import java.util.List;
import java.util.Optional;


@Service
@Transactional
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private GatewayClient gatewayClient;

    @Autowired
    private PaymentCallbackService paymentCallbackService;

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
            processPaymentAsync(savedPayment.getId());

            return savedPayment;

        } catch (Exception e) {
            log.error("ERROR CREATING PAYMENT: {}", e.getMessage(), e);
            throw new RuntimeException("Payment creation failed: " + e.getMessage());
        }
    }

    @Async("paymentProcessorExecutor")
    @Retryable(
            value = {Exception.class}, // Повторяем при любых ошибках
            maxAttempts = 3,          // 3 попытки
            backoff = @Backoff(delay = 2000, multiplier = 2) // Задержка 2сек, 4сек, 8сек
    )
    public void processPaymentAsync(Long paymentId) {
        try {
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

            log.info("PROCESSING PAYMENT - ID: {}, CardToken: {}",
                    payment.getPaymentId(), payment.getCardToken());

            payment.setStatus(PaymentStatus.PROCESSING);
            paymentRepository.save(payment);

            log.info("PROCESSING PAYMENT THROUGH GATEWAY: {}", payment.getPaymentId());

            // Создаем запрос к шлюзу с токеном
            GatewayChargeRequest chargeRequest = new GatewayChargeRequest();
            chargeRequest.setPaymentId(payment.getPaymentId());
            chargeRequest.setAmount(payment.getAmount());
            chargeRequest.setCurrency(payment.getCurrency());
            chargeRequest.setCardToken(payment.getCardToken());
            chargeRequest.setDescription(payment.getDescription());

            // Вызываем шлюз
            GatewayResponse gatewayResponse = gatewayClient.charge(chargeRequest);

            // Обновляем платеж на основе ответа от шлюза
            if ("SUCCESS".equals(gatewayResponse.getStatus())) {
                payment.setStatus(PaymentStatus.SUCCESS);
                paymentRepository.save(payment);
                payment.setTransactionId(gatewayResponse.getTransactionId());
                log.info("PAYMENT COMPLETE SUCCESSFULLY: {}", payment.getPaymentId());
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setErrorMessage(gatewayResponse.getErrorMessage());
                paymentRepository.save(payment);
                log.warn("PAYMENT FAILED: {}", payment.getPaymentId());

                if (isRetryableError(gatewayResponse.getErrorMessage())) {
                    throw new RuntimeException("Bank error: " + gatewayResponse.getErrorMessage());
                }

            }

            if (payment.getStatus() == PaymentStatus.SUCCESS ||
                    !isRetryableError(payment.getErrorMessage())) {

                String callbackStatus = payment.getStatus() == PaymentStatus.SUCCESS ? "COMPLETED" : "FAILED";
                paymentCallbackService.sendPaymentCallback(
                        payment.getOrderId(),
                        payment.getPaymentId(),
                        callbackStatus,
                        payment.getErrorMessage()
                );
            }

        } catch (Exception e) {
            log.error("ERROR PROCESSING PAYMENT {}: {}", paymentId, e.getMessage());
            throw e;
            // Обновляем статус платежа на FAILED в случае ошибки
//            paymentRepository.findById(paymentId).ifPresent(p -> {
//                p.setStatus(PaymentStatus.FAILED);
//                p.setErrorMessage("Processing error: " + e.getMessage());
//                paymentRepository.save(p);
//
//                // Callback об ошибке
//                paymentCallbackService.sendPaymentCallback(
//                        p.getOrderId(),
//                        p.getPaymentId(),
//                        "FAILED",
//                        p.getErrorMessage()
//                );
//            });
        }


    }

    private boolean isRetryableError(String errorMessage) {
        if (errorMessage == null) return false;

        String lowerError = errorMessage.toLowerCase();
        return lowerError.contains("timeout") ||
                lowerError.contains("busy") ||
                lowerError.contains("temporarily") ||
                lowerError.contains("unavailable") ||
                lowerError.contains("try again") ||
                lowerError.contains("банк временно недоступен") ||
                lowerError.contains("повторите попытку");
    }

    // Этот метод вызовется после всех неудачных попыток
    @Recover
    public void processPaymentRecover(Exception e, Long paymentId) {
        log.error("ALL RETRY ATTEMPTS FAILED for payment: {}", paymentId);

        // Устанавливаем окончательный статус FAILED
        paymentRepository.findById(paymentId).ifPresent(payment -> {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setErrorMessage("Bank unavailable after 3 attempts: " + e.getMessage());
            paymentRepository.save(payment);

            // Отправляем финальный callback
            paymentCallbackService.sendPaymentCallback(
                    payment.getOrderId(),
                    payment.getPaymentId(),
                    "FAILED",
                    payment.getErrorMessage()
            );
        });
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