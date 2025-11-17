package com.example.paymentgateway.client;

import com.example.paymentgateway.model.GatewayChargeRequest;
import com.example.paymentgateway.model.GatewayResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "payment-gateway", url = "payment-gateway:8080")
public interface GatewayClient {

    @PostMapping("/api/gateway/charges")
    GatewayResponse charge(@RequestBody GatewayChargeRequest chargeRequest);
}