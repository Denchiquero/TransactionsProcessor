package com.example.orderservice.service;

import com.example.orderservice.client.*;
import com.example.orderservice.model.*;
import com.example.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentServiceClient paymentServiceClient;

    @Autowired
    private ReportServiceClient reportServiceClient;

    public Order createOrder(OrderRequest orderRequest) {
        log.info("Creating order for customer: {}", orderRequest.getCustomerEmail());

        if (orderRequest.getCardToken() == null || orderRequest.getCardToken().trim().isEmpty()) {
            throw new IllegalArgumentException("Card token is required");
        }

        Order order = new Order();
        order.setCustomerEmail(orderRequest.getCustomerEmail());
        order.setCustomerName(orderRequest.getCustomerName());
        order.setCustomerPhone(orderRequest.getCustomerPhone());
        order.setShippingAddress(orderRequest.getShippingAddress());
        order.setCardToken(orderRequest.getCardToken());
        order.setStatus(OrderStatus.PAYMENT_PROCESSING);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : orderRequest.getItems()) {
            OrderItem item = new OrderItem();
            item.setProductId(itemRequest.getProductId());
            item.setProductName(itemRequest.getProductName());
            item.setQuantity(itemRequest.getQuantity());
            item.setPrice(itemRequest.getPrice());
            order.getItems().add(item);

            BigDecimal itemTotal = itemRequest.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);
        log.info("Order created for payment processing: {}", savedOrder.getOrderId());

        try {
            PaymentResponse paymentResponse = processPayment(savedOrder);

            log.info("Payment response for order: {}, {}", paymentResponse.getStatus(), savedOrder.getOrderId());

            if ("COMPLETED".equals(paymentResponse.getStatus())) {
                savedOrder.setPaymentId(paymentResponse.getPaymentId());
                savedOrder.setStatus(OrderStatus.PAYMENT_COMPLETED);

                Order completedOrder = orderRepository.save(savedOrder);
                log.info("Order completed successfully: {}", completedOrder.getOrderId());
//                orderRepository.flush();
                return completedOrder;
            } else {
                orderRepository.delete(savedOrder);
                String errorMsg = paymentResponse.getErrorMessage() != null ?
                        paymentResponse.getErrorMessage() : "Payment failed";
                log.error("Payment failed, order deleted: {}", savedOrder.getOrderId());
                throw new PaymentFailedException(errorMsg);
            }
        } catch (Exception e) {
            orderRepository.delete(savedOrder);
            log.error("Payment processing failed, order deleted: {}", savedOrder.getOrderId(), e);
            throw new PaymentFailedException("Payment processing failed: " + e.getMessage());
        }
    }

    @Async
    public void notifyReportServiceAsync(String orderId) {
        try {
            reportServiceClient.sendOrderConfirmation(orderId);
            log.info("Notification sent to report-service for order: {}", orderId);
        } catch (Exception e) {
            log.error("Error during notification report-service: {}", e.getMessage());
        }
    }

    private PaymentResponse processPayment(Order order) {
        try {
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setOrderId(order.getOrderId());
            paymentRequest.setAmount(order.getTotalAmount());
            paymentRequest.setCardToken(order.getCardToken());
            paymentRequest.setCurrency("RUB");
            paymentRequest.setCustomerEmail(order.getCustomerEmail());
            paymentRequest.setDescription("Payment for order: " + order.getOrderId());

            log.info("Calling SYNC payment service");

            PaymentResponse paymentResponse = paymentServiceClient.createPayment(paymentRequest);
            return paymentResponse;

        } catch (Exception e) {
            PaymentResponse errorResponse = new PaymentResponse();
            errorResponse.setStatus("FAILED");
            errorResponse.setErrorMessage("Payment service error: " + e.getMessage());
            return errorResponse;
        }
    }

    // Кастомное исключение для неудачного платежа
    public static class PaymentFailedException extends RuntimeException {
        public PaymentFailedException(String message) {
            super(message);
        }
    }


    @Transactional
    public void processPaymentCallback(String orderId, String status, String paymentId, String errorMessage) {
        log.info("Processing payment callback for order: {}, status: {}", orderId, status);

        Optional<Order> orderOpt = findOrderWithRetry(orderId);

        if (orderOpt.isEmpty()) {
            log.error("Order not found: {}", orderId);
            return;
        }

        Order order = orderOpt.get();

        if ("COMPLETED".equals(status)) {
            order.setPaymentId(paymentId);
            order.setStatus(OrderStatus.PAYMENT_COMPLETED);
            orderRepository.save(order);
            log.info("Order payment completed: {}", orderId);

        } else {
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            orderRepository.save(order);
            log.warn("Order payment failed: {}, error: {}", orderId, errorMessage);
        }
    }

    private Optional<Order> findOrderWithRetry(String orderId) {
        int attempts = 0;
        while (attempts < 10) {
            Optional<Order> orderOpt = orderRepository.findByOrderId(orderId);
            if (orderOpt.isPresent()) {
                return orderOpt;
            }
            attempts++;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return Optional.empty();
    }

    public boolean retryPayment(String orderId) {
        Optional<Order> orderOpt = orderRepository.findByOrderId(orderId);
        if (orderOpt.isEmpty()) {
            return false;
        }

        Order order = orderOpt.get();
        if (order.getStatus() != OrderStatus.PAYMENT_FAILED &&
                order.getStatus() != OrderStatus.PENDING) {
            log.warn("Cannot retry payment for order in status: {}", order.getStatus());
            return false;
        }

        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);

        processPayment(order);
        return true;
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Order> getOrderByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByCustomerEmail(String customerEmail) {
        return orderRepository.findByCustomerEmail(customerEmail);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public Order updateOrderStatus(String orderId, OrderStatus status) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        order.setStatus(status);
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public long getTotalOrdersCount() {
        return orderRepository.count();
    }
}