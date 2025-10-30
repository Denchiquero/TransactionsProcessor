package com.example.paymentservice.client;

import com.example.paymentservice.model.GatewayChargeRequest;
import com.example.paymentservice.model.GatewayResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "payment-gateway", url = "${gateway.service.url:http://localhost:8082}")
public interface GatewayClient {

    @PostMapping("/api/v1/charges")
    GatewayResponse charge(@RequestBody GatewayChargeRequest chargeRequest);
}