package com.example.paymentservice;

import com.example.paymentservice.model.Payment;
import com.example.paymentservice.model.PaymentStatus;
import com.example.paymentservice.repository.PaymentRepository;
import com.example.paymentservice.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void getAllPayments_ShouldReturnAllPayments() {
        // Given
        Payment payment1 = new Payment();
        Payment payment2 = new Payment();
        when(paymentRepository.findAll()).thenReturn(Arrays.asList(payment1, payment2));

        // When
        List<Payment> payments = paymentService.getAllPayments();

        // Then
        assertNotNull(payments);
        assertEquals(2, payments.size());
        verify(paymentRepository, times(1)).findAll();
    }

    @Test
    void getPaymentById_ShouldReturnPayment_WhenExists() {
        // Given
        Long paymentId = 1L;
        Payment payment = new Payment();
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // When
        Optional<Payment> result = paymentService.getPaymentById(paymentId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(payment, result.get());
    }

    @Test
    void getPaymentById_ShouldReturnEmpty_WhenNotExists() {
        // Given
        Long paymentId = 999L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // When
        Optional<Payment> result = paymentService.getPaymentById(paymentId);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void getTotalPaymentsCount_ShouldReturnCount() {
        // Given
        when(paymentRepository.count()).thenReturn(5L);

        // When
        long count = paymentService.getTotalPaymentsCount();

        // Then
        assertEquals(5L, count);
        verify(paymentRepository, times(1)).count();
    }
}