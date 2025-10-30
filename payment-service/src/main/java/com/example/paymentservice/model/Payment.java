package com.example.paymentservice.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String paymentId;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private String customerEmail;
    @Column(name = "card_token")
    private String cardToken;
    private String description;
    private String transactionId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Payment() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (paymentId == null) {
            paymentId = "pay_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String toString() {
        return "Payment(id=" + this.getId() + ", paymentId=" + this.getPaymentId() + ", orderId=" + this.getOrderId() + ", amount=" + this.getAmount() + ", currency=" + this.getCurrency() + ", status=" + this.getStatus() + ", customerEmail=" + this.getCustomerEmail() + ", cardMask=" + this.getCardToken() + ", description=" + this.getDescription() + ", transactionId=" + this.getTransactionId() + ", errorMessage=" + this.getErrorMessage() + ", createdAt=" + this.getCreatedAt() + ", updatedAt=" + this.getUpdatedAt() + ")";
    }

    public void setId(Long id) { this.id = id; }

    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public void setOrderId(String orderId) { this.orderId = orderId; }

    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public void setCurrency(String currency) { this.currency = currency; }

    public void setStatus(PaymentStatus status) { this.status = status; }

    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public void setCardToken(String cardToken) { this.cardToken = cardToken; }

    public void setDescription(String description) { this.description = description; }

    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getId() { return id; }

    public String getPaymentId() { return paymentId; }

    public String getOrderId() { return orderId; }

    public BigDecimal getAmount() { return amount; }

    public String getCurrency() { return currency; }

    public PaymentStatus getStatus() { return status; }

    public String getCustomerEmail() { return customerEmail; }

    public String getCardToken() { return cardToken; }

    public String getDescription() { return description; }

    public String getTransactionId() { return transactionId; }

    public String getErrorMessage() { return errorMessage; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}