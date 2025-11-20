package com.example.paymentservice;

import com.example.paymentservice.client.GatewayClient;
import com.example.paymentservice.model.*;
import com.example.paymentservice.repository.PaymentRepository;
import com.example.paymentservice.service.PaymentCallbackService;
import com.example.paymentservice.service.PaymentProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentProcessorTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private GatewayClient gatewayClient;

    @Mock
    private PaymentCallbackService paymentCallbackService;

    @InjectMocks
    private PaymentProcessor paymentProcessor;

    @Test
    void createSuccessResponse_ShouldReturnCorrectResponse() {
        // Given
        Payment payment = new Payment();
        payment.setPaymentId("pay_123");
        payment.setOrderId("order_456");
        payment.setAmount(new BigDecimal("100.00"));

        // When
        PaymentResponse response = paymentProcessor.createSuccessResponse(payment);

        // Then
        assertNotNull(response);
        assertEquals("pay_123", response.getPaymentId());
        assertEquals("order_456", response.getOrderId());
        assertEquals("COMPLETED", response.getStatus());
        assertNull(response.getErrorMessage());
    }

    @Test
    void createFailedResponse_ShouldReturnErrorResponse() {
        // Given
        Payment payment = new Payment();
        payment.setPaymentId("pay_123");
        payment.setOrderId("order_456");
        String errorMessage = "Payment declined";

        // When
        PaymentResponse response = paymentProcessor.createFailedResponse(payment, errorMessage);

        // Then
        assertNotNull(response);
        assertEquals("pay_123", response.getPaymentId());
        assertEquals("order_456", response.getOrderId());
        assertEquals("FAILED", response.getStatus());
        assertEquals("Payment declined", response.getErrorMessage());
    }

    @Test
    void isRetryableError_ShouldReturnTrue_ForRetryableErrors() {
        // When & Then
        assertTrue(paymentProcessor.isRetryableError("timeout error"));
        assertTrue(paymentProcessor.isRetryableError("bank busy"));
        assertTrue(paymentProcessor.isRetryableError("temporarily unavailable"));
        assertTrue(paymentProcessor.isRetryableError("try again later"));
        assertTrue(paymentProcessor.isRetryableError("payment declined by bank"));
        assertTrue(paymentProcessor.isRetryableError("повторите попытку"));
    }

    @Test
    void isRetryableError_ShouldReturnFalse_ForNonRetryableErrors() {
        // When & Then
        assertFalse(paymentProcessor.isRetryableError("insufficient funds"));
        assertFalse(paymentProcessor.isRetryableError("invalid card"));
        assertFalse(paymentProcessor.isRetryableError("card expired"));
        assertFalse(paymentProcessor.isRetryableError(null));
    }
}