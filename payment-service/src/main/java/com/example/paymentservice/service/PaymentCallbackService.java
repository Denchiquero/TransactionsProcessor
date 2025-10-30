// [file name]: PaymentCallbackService.java
package com.example.paymentservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentCallbackService {

    private static final Logger log = LoggerFactory.getLogger(PaymentCallbackService.class);

    @Value("${order.service.url:http://localhost:8081}")
    private String orderServiceUrl;

    @Autowired
    private RestTemplate restTemplate;

    public void sendPaymentCallback(String orderId, String paymentId, String status, String errorMessage) {
        try {
            String callbackUrl = orderServiceUrl + "/api/orders/" + orderId + "/payment-callback";

            Map<String, String> callbackRequest = new HashMap<>();
            callbackRequest.put("paymentId", paymentId);
            callbackRequest.put("status", status);
            callbackRequest.put("errorMessage", errorMessage);

            restTemplate.postForEntity(callbackUrl, callbackRequest, Void.class);
            log.info("Payment callback sent to order service for order: {}", orderId);

        } catch (Exception e) {
            log.error("Failed to send payment callback for order: {}", orderId, e);
            // Можно добавить retry логику здесь
        }
    }
}