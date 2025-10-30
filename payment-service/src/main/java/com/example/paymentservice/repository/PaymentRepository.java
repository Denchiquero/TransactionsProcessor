package com.example.paymentservice.repository;

import com.example.paymentservice.model.Payment;
import com.example.paymentservice.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentId(String paymentId);

    List<Payment> findByOrderId(String orderId);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByCustomerEmail(String customerEmail);

    @Query("SELECT p FROM Payment p WHERE p.createdAt >= CURRENT_DATE")
    List<Payment> findTodayPayments();

    boolean existsByOrderId(String orderId);
}