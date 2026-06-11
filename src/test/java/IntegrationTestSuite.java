// src/test/java/org/example/IntegrationTestSuite.java


import org.example.PortfolioApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = PortfolioApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class IntegrationTestSuite {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextLoads() {
        assertThat(restTemplate).isNotNull();
    }

    @Test
    void whenCallHealthEndpoint_thenApplicationIsRunning() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/health", String.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }
}