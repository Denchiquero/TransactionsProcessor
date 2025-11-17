//package com.example.orderservice.client;
//
//import com.example.orderservice.model.OrderDTO;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//
//@FeignClient(name = "order-service", url = "order-service:8080")
//public interface OrderServiceClient {
//
//    @GetMapping("/api/orders/order/{orderId}")
//    OrderDTO getOrderByOrderId(@PathVariable String orderId);
//}