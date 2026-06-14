package com.example.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class ApiController {
    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

   @GetMapping("/")
    public ResponseEntity<Map<String, Object>> welcome() {
        logger.info("Welcome endpoint called");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to Spring Boot REST API!");
        response.put("version", "1.0.0");
        response.put("endpoints", Map.of(
            "health", "/api/health",
            "hello", "/api/hello/{name}",
            "users", "/api/users"
        ));
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        logger.info("Health check endpoint called");
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Spring Boot Application");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/hello/{name}")
    public ResponseEntity<Map<String, String>> hello(@PathVariable String name) {
        logger.info("Hello endpoint called with name: {}", name);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello, " + name);
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
}