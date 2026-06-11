package org.example;

// src/main/java/org/example/portfolio/PortfolioApplication.java


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PortfolioApplication {
    public static void main(String[] args) {
        SpringApplication.run(PortfolioApplication.class, args);
        System.out.println("""
                
                ╔════════════════════════════════════════════════════╗
                ║   🚀 Portfolio Backend Started!                    ║
                ║   📝 API: http://localhost:8080                    ║
                ║   🗄️  H2 Console: http://localhost:8080/h2-console ║
                ╚════════════════════════════════════════════════════╝
                """);
    }
}