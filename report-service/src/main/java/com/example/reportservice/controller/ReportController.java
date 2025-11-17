// [file name]: ReportController.java
package com.example.reportservice.controller;

import com.example.reportservice.model.EmailRequest;
import com.example.reportservice.service.EmailService;
import com.example.reportservice.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/orders/{orderId}/confirm")
    public ResponseEntity<String> sendOrderConfirmation(@PathVariable String orderId) {
        try {
            reportService.sendOrderConfirmation(orderId);
            return ResponseEntity.ok("Подтверждение заказа отправлено");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка отправки подтверждения: " + e.getMessage());
        }
    }

    @PostMapping("/email/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest emailRequest) {
        try {
            emailService.sendEmail(emailRequest);
            return ResponseEntity.ok("Email отправлен успешно");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка отправки email: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Report Service is healthy");
    }
}