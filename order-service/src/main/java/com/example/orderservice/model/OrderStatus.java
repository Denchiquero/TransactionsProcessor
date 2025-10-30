package com.example.orderservice.model;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PAYMENT_PENDING,
    PAYMENT_PROCESSING,
    PAYMENT_COMPLETED,
    PAYMENT_FAILED,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED
}