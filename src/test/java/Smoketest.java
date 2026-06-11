// src/test/java/org/example/SimpleSmokeTest.java
package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = PortfolioApplication.class)
class SimpleSmokeTest {

    @Test
    void contextLoads() {
        System.out.println("✅ Context loaded successfully!");
    }
}