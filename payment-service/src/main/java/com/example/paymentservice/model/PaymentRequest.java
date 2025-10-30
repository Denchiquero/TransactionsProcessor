package com.example.paymentservice.model;

import java.math.BigDecimal;

public class PaymentRequest {
    private BigDecimal amount;
    private String description;
    private String orderId;
    private String currency;
    private String customerEmail;
    private String cardToken;

    public PaymentRequest() {
    }

    public PaymentRequest(BigDecimal amount, String description) {
        this.amount = amount;
        this.description = description;
        this.currency = "RUB";
    }

    public PaymentRequest(String orderId, BigDecimal amount, String currency, String customerEmail, String description, String cardToken) {
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.customerEmail = customerEmail;
        this.description = description;
        this.cardToken = cardToken;
    }

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCardToken() { return cardToken; }
    public void setCardToken(String cardToken) { this.cardToken = cardToken; }
}