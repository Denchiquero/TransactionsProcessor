package com.example.paymentgateway.service;

import com.example.paymentgateway.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentGatewayService {

    private static final Logger log = LoggerFactory.getLogger(PaymentGatewayService.class);
    private final Map<String, CardData> tokenStorage = new ConcurrentHashMap<>();
    private final Random random = new Random();

    // НОВЫЙ метод для создания токена
    public TokenResponse createToken(TokenRequest request) {

        // Создаем токен
        String token = "tok_" + System.currentTimeMillis() + "_" + random.nextInt(1000);

        // Сохраняем данные карты
        CardData cardData = new CardData(
                request.getCardNumber(),
                request.getExpiryMonth(),
                request.getExpiryYear(),
                request.getCvv(),
                request.getCardholderName()
        );

        tokenStorage.put(token, cardData);
        log.info("Token created: {} for card: {}", token,
                maskCardNumber(request.getCardNumber()));

        return new TokenResponse(token, "CREATED", "Token successfully created");
    }

    // ОБНОВЛЕННЫЙ метод для списания средств
    public GatewayResponse processCharge(GatewayChargeRequest request) {
        // Находим карту по токену
        CardData cardData = tokenStorage.get(request.getCardToken());
        if (cardData == null) {
            throw new IllegalArgumentException("Invalid card token");
        }

        log.info("Processing charge with token: {} for card: {}",
                request.getCardToken(), maskCardNumber(cardData.getCardNumber()));

        // Имитация обработки платежа
        simulateNetworkDelay();

        GatewayResponse response = new GatewayResponse();
        boolean isSuccess = random.nextDouble() < 0.9; // 90% успеха

        if (isSuccess) {
            response.setStatus("SUCCESS");
            response.setPaymentId("pay_" + System.currentTimeMillis());
            response.setTransactionId("txn_" + System.currentTimeMillis());
            log.info("Charge successful for token: {}", request.getCardToken());
        } else {
            response.setStatus("FAILED");
            response.setErrorMessage("Payment declined by bank");
            log.warn("Charge failed for token: {}", request.getCardToken());
        }

        return response;
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) return "****";
        return "****" + cardNumber.substring(cardNumber.length() - 4);
    }

    private void simulateNetworkDelay() {
        try {
            Thread.sleep(1000 + random.nextInt(4000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Внутренний класс для хранения данных карты
    private static class CardData {
        private final String cardNumber;
        private final int expiryMonth;
        private final int expiryYear;
        private final String cvv;
        private final String cardholderName;

        public CardData(String cardNumber, int expiryMonth, int expiryYear,
                        String cvv, String cardholderName) {
            this.cardNumber = cardNumber;
            this.expiryMonth = expiryMonth;
            this.expiryYear = expiryYear;
            this.cvv = cvv;
            this.cardholderName = cardholderName;
        }

        // Getters
        public String getCardNumber() { return cardNumber; }
        public int getExpiryMonth() { return expiryMonth; }
        public int getExpiryYear() { return expiryYear; }
        public String getCvv() { return cvv; }
        public String getCardholderName() { return cardholderName; }
    }
}