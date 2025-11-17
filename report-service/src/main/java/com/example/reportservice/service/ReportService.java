package com.example.reportservice.service;

import com.example.reportservice.client.OrderServiceClient;
import com.example.reportservice.model.OrderDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    @Autowired
    private OrderServiceClient orderServiceClient;

    @Autowired
    private EmailService emailService;

    public void sendOrderConfirmation(String orderId) {
        try {
            log.info("Отправка подтверждения заказа: {}", orderId);

            OrderDTO order = orderServiceClient.getOrderByOrderId(orderId);

            if (order != null) {
                emailService.sendOrderConfirmationEmail(order);
                log.info("Уведомление о подтверждении заказа отправлено для: {}", orderId);
            } else {
                log.error("Заказ не найден: {}", orderId);
                throw new RuntimeException("Order not found: " + orderId);
            }

        } catch (Exception e) {
            log.error("Ошибка при отправке уведомления для заказа: {}", orderId, e);
            throw new RuntimeException("Failed to send order confirmation: " + e.getMessage(), e);
        }
    }
}
