package com.example.orderservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    @GetMapping("/")
    public String frontend() {
        return "forward:/index.html";
    }

    @GetMapping("/orders")
    public String ordersPage() {
        return "forward:/index.html";
    }
}