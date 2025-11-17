//package com.example.reportservice.client;
//
//
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//
//@FeignClient(name = "report-service", url = "report-service:8080")
//public interface ReportServiceClient {
//
//    @PostMapping("/api/reports/orders/{orderId}/confirm")
//    void sendOrderConfirmation(@PathVariable String orderId);
//}