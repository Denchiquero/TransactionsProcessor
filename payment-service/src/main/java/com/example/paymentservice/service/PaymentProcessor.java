package com.example.paymentservice.service;

import com.example.paymentservice.repository.PaymentRepository;
import com.example.paymentservice.client.GatewayClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.paymentservice.model.*;

@Service
public class PaymentProcessor {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessor.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private GatewayClient gatewayClient;

    @Autowired
    private PaymentCallbackService paymentCallbackService;

    @Async("paymentProcessorExecutor")
    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void processPaymentAsync(Long paymentId) {
        log.info("ASYNC PAYMENT PROCESSING STARTED: {}", paymentId);

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
                payment.setTransactionId(gatewayResponse.getTransactionId());
                paymentRepository.save(payment);
                log.info("PAYMENT COMPLETE SUCCESSFULLY: {}", payment.getPaymentId());

                // Отправляем callback об успехе
                paymentCallbackService.sendPaymentCallback(
                        payment.getOrderId(),
                        payment.getPaymentId(),
                        "COMPLETED",
                        null
                );

            } else {
                // Проверяем, нужно ли повторять
                if (isRetryableError(gatewayResponse.getErrorMessage())) {
                    log.warn("RETRYABLE ERROR - Payment {}: {}", paymentId, gatewayResponse.getErrorMessage());
                    throw new RuntimeException("Bank error: " + gatewayResponse.getErrorMessage());
                } else {
                    // НЕ retryable ошибка - завершаем окончательно
                    payment.setStatus(PaymentStatus.FAILED);
                    payment.setErrorMessage(gatewayResponse.getErrorMessage());
                    paymentRepository.save(payment);
                    log.warn("FINAL PAYMENT FAILED: {}", payment.getPaymentId());

                    // Отправляем callback для окончательной ошибки
                    paymentCallbackService.sendPaymentCallback(
                            payment.getOrderId(),
                            payment.getPaymentId(),
                            "FAILED",
                            payment.getErrorMessage()
                    );
                }
            }

        } catch (Exception e) {
            log.error("ERROR PROCESSING PAYMENT {}: {}", paymentId, e.getMessage());

            // Для retryable ошибок - просто пробрасываем исключение
            if (isRetryableError(e.getMessage())) {
                throw e;
            }

            // Для НЕ retryable ошибок - сохраняем статус FAILED
            paymentRepository.findById(paymentId).ifPresent(payment -> {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setErrorMessage("Processing error: " + e.getMessage());
                paymentRepository.save(payment);

                paymentCallbackService.sendPaymentCallback(
                        payment.getOrderId(),
                        payment.getPaymentId(),
                        "FAILED",
                        payment.getErrorMessage()
                );
            });
        }
    }


    public PaymentResponse processPaymentImmediately(PaymentRequest paymentRequest) {
        log.info("SYNC PAYMENT PROCESSING STARTED for order: {}", paymentRequest.getOrderId());

        // Создаем платеж
        Payment payment = new Payment();
        payment.setOrderId(paymentRequest.getOrderId());
        payment.setAmount(paymentRequest.getAmount());
        payment.setCardToken(paymentRequest.getCardToken());
        payment.setCurrency(paymentRequest.getCurrency());
        payment.setCustomerEmail(paymentRequest.getCustomerEmail());
        payment.setDescription(paymentRequest.getDescription());
        payment.setStatus(PaymentStatus.PROCESSING);

        Payment savedPayment = paymentRepository.save(payment);
        log.info("PAYMENT CREATED: {}", savedPayment.getPaymentId());

        try {
            // Сразу обрабатываем через шлюз СИНХРОННО
            GatewayChargeRequest chargeRequest = new GatewayChargeRequest();
            chargeRequest.setPaymentId(savedPayment.getPaymentId());
            chargeRequest.setAmount(payment.getAmount());
            chargeRequest.setCurrency(payment.getCurrency());
            chargeRequest.setCardToken(payment.getCardToken());
            chargeRequest.setDescription(payment.getDescription());

            log.info("PROCESSING PAYMENT THROUGH GATEWAY: {}", savedPayment.getPaymentId());
            GatewayResponse gatewayResponse = gatewayClient.charge(chargeRequest);

            // Обновляем статус платежа
            if ("SUCCESS".equals(gatewayResponse.getStatus())) {
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setTransactionId(gatewayResponse.getTransactionId());
                paymentRepository.save(payment);
                log.info("PAYMENT COMPLETED SUCCESSFULLY: {}", savedPayment.getPaymentId());

                // Сразу отправляем callback
                paymentCallbackService.sendPaymentCallback(
                        payment.getOrderId(),
                        payment.getPaymentId(),
                        "COMPLETED",
                        null
                );

                return createSuccessResponse(payment);

            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setErrorMessage(gatewayResponse.getErrorMessage());
                paymentRepository.save(payment);
                log.warn("PAYMENT FAILED: {}", savedPayment.getPaymentId());

                return createFailedResponse(payment, gatewayResponse.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("ERROR PROCESSING PAYMENT {}: {}", savedPayment.getPaymentId(), e.getMessage());
            payment.setStatus(PaymentStatus.FAILED);
            payment.setErrorMessage("Processing error: " + e.getMessage());
            paymentRepository.save(payment);

            return createFailedResponse(payment, "Processing error: " + e.getMessage());
        }
    }

    private PaymentResponse createSuccessResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(payment.getPaymentId());
        response.setStatus("COMPLETED");
        response.setOrderId(payment.getOrderId());
        return response;
    }

    private PaymentResponse createFailedResponse(Payment payment, String errorMessage) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(payment.getPaymentId());
        response.setStatus("FAILED");
        response.setOrderId(payment.getOrderId());
        response.setErrorMessage(errorMessage);
        return response;
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

    private boolean isRetryableError(String errorMessage) {
        if (errorMessage == null) return false;

        String lowerError = errorMessage.toLowerCase();
        return lowerError.contains("timeout") ||
                lowerError.contains("busy") ||
                lowerError.contains("temporarily") ||
                lowerError.contains("unavailable") ||
                lowerError.contains("try again") ||
                lowerError.contains("payment declined by bank") ||
                lowerError.contains("повторите попытку");
    }
}

