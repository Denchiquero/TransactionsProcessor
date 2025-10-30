package com.example.paymentgateway.model;

public class GatewayResponse {
    private String paymentId;
    private String orderId;
    private String status;
    private String transactionId;
    private String errorMessage;

    // Конструкторы
    public GatewayResponse() {}

    public GatewayResponse(String status, String errorMessage) {
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public GatewayResponse(String paymentId, String status, String transactionId) {
        this.paymentId = paymentId;
        this.status = status;
        this.transactionId = transactionId;
    }

    // Getters and Setters
    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "GatewayResponse{" +
                "paymentId='" + paymentId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", status='" + status + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}