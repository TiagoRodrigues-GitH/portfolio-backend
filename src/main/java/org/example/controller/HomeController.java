package org.example.controller;
// src/main/java/org/example/controller/HomeController.java


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
                "message", "Portfolio API is running!",
                "status", "OK",
                "endpoints", "Available endpoints: /api/health, /api/projects, /api/test/hello"
        );
    }

    @GetMapping("/api/health")
    public String health() {
        return "OK";
    }
}