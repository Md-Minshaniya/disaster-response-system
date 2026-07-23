package com.disaster.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    /**
     * Serve dashboard.html on root path
     */
    @GetMapping("/")
    public String dashboard() {
        return "dashboard";
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public String health() {
        return "{\"status\": \"UP\"}";
    }
}
