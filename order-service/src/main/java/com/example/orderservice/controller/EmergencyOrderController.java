package com.example.orderservice.controller;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
public class EmergencyOrderController {

    @PostMapping("/api/order")
    public Map<String, String> createOrder(@RequestBody Map<String, Object> request) {
        System.out.println("EMERGENCY ORDER RECEIVED: " + request);

        Map<String, String> response = new HashMap<>();
        response.put("orderId", "EMERGENCY_" + System.currentTimeMillis());
        response.put("status", "SUCCESS");
        response.put("message", "Order created via EmergencyController");

        return response;
    }

    @GetMapping("/api/order/emergency")
    public String test() {
        return "Emergency Controller is WORKING!";
    }
}