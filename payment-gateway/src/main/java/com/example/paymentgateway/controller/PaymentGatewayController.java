package com.example.paymentgateway.controller;

import com.example.paymentgateway.model.*;
import com.example.paymentgateway.service.PaymentGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gateway")
public class PaymentGatewayController {

    private static final Logger log = LoggerFactory.getLogger(PaymentGatewayController.class);

    @Autowired
    private PaymentGatewayService paymentGatewayService;

    // НОВЫЙ endpoint для получения токена
    @PostMapping("/tokens")
    public ResponseEntity<TokenResponse> createToken(@RequestBody TokenRequest request) {
        try {
            log.info("Creating token for card ending with: {}",
                    request.getCardNumber().substring(request.getCardNumber().length() - 4));

            TokenResponse response = paymentGatewayService.createToken(request);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Token creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new TokenResponse(null, "ERROR", e.getMessage()));
        }
    }

    // Существующий endpoint для списания средств (ОБНОВЛЯЕМ)
    @PostMapping("/charges")
    public ResponseEntity<GatewayResponse> charge(@RequestBody GatewayChargeRequest request) {
        try {
            log.info("Processing charge for payment: {} with token", request.getPaymentId());

            GatewayResponse response = paymentGatewayService.processCharge(request);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Charge processing failed: {}", e.getMessage());
            GatewayResponse errorResponse = new GatewayResponse();
            errorResponse.setStatus("ERROR");
            errorResponse.setErrorMessage(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}