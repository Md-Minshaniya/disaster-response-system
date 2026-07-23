package com.disaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = "com.disaster")
public class DisasterResponseApplication {

    public static void main(String[] args) {
        SpringApplication.run(DisasterResponseApplication.class, args);
        log.info("╔════════════════════════════════════════════════════════╗");
        log.info("║     DISASTER RESPONSE SYSTEM STARTED SUCCESSFULLY      ║");
        log.info("║          Multi-Agent Intelligent Management           ║");
        log.info("║          MySQL Database: disaster_db                 ║");
        log.info("║          Access Dashboard: http://localhost:8080     ║");
        log.info("║          API Documentation: /swagger-ui.html         ║");
        log.info("╚════════════════════════════════════════════════════════╝");
    }
}
