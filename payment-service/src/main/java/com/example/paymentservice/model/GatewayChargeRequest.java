package com.example.paymentservice.model;

import java.math.BigDecimal;

public class GatewayChargeRequest {
    private String paymentId;
    private BigDecimal amount;
    private String currency;
    private String cardToken;
    private String description;

    // Конструкторы
    public GatewayChargeRequest() {}

    // Getters/Setters
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getCardToken() { return cardToken; }
    public void setCardToken(String cardToken) { this.cardToken = cardToken; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}