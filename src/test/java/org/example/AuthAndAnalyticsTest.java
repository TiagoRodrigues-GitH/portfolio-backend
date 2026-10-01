package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.analytics.VisitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.admin.email=admin@example.org",
        "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "app.cors.allowed-origins=https://tiagorodrigues-gith.github.io",
})
@AutoConfigureMockMvc
class AuthAndAnalyticsTest {

    private static final String PASSWORD = "correct horse battery staple";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private VisitRepository visits;

    @DynamicPropertySource
    static void adminHash(DynamicPropertyRegistry registry) {
        registry.add("app.admin.password-hash", () -> new BCryptPasswordEncoder(4).encode(PASSWORD));
    }

    @BeforeEach
    void clean() {
        visits.deleteAll();
    }

    private String login(String password, String ip) throws Exception {
        String body = json.writeValueAsString(new LoginBody("admin@example.org", password));
        var result = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body)
                .with(r -> { r.setRemoteAddr(ip); return r; })).andReturn();
        if (result.getResponse().getStatus() != 200) {
            return "status:" + result.getResponse().getStatus();
        }
        JsonNode node = json.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }

    record LoginBody(String email, String password) { }

    @Test
    void adminEndpointsNeedAToken() throws Exception {
        mvc.perform(get("/api/admin/analytics/summary")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/projects").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/projects")).andExpect(status().isOk());
    }

    @Test
    void loginIssuesAWorkingTokenAndRejectsWrongPasswords() throws Exception {
        assertThat(login("wrong", "10.0.0.1")).isEqualTo("status:401");
        String token = login(PASSWORD, "10.0.0.1");
        assertThat(token).doesNotStartWith("status:");
        mvc.perform(get("/api/admin/analytics/summary").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.days").value(30));
        mvc.perform(get("/api/admin/analytics/summary").header("Authorization", "Bearer " + token + "x"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void repeatedFailuresAreRateLimited() throws Exception {
        for (int i = 0; i < 5; i++) {
            assertThat(login("wrong", "10.0.0.9")).isEqualTo("status:401");
        }
        assertThat(login(PASSWORD, "10.0.0.9")).isEqualTo("status:429");   // even the right password
        assertThat(login(PASSWORD, "10.0.0.10")).doesNotStartWith("status:"); // other clients unaffected
    }

    @Test
    void visitsAreRecordedWithTheConnectionIpUnlessTheBrowserOptsOut() throws Exception {
        String visit = "{\"path\":\"/projects\",\"lang\":\"pt\",\"referrer\":\"https://www.linkedin.com/feed/\",\"screen\":\"1920x1080\",\"timezone\":\"America/Sao_Paulo\",\"session\":\"abc\"}";
        mvc.perform(post("/api/analytics/visit").contentType(MediaType.APPLICATION_JSON).content(visit)
                .with(r -> { r.setRemoteAddr("203.0.113.7"); return r; })).andExpect(status().isNoContent());
        mvc.perform(post("/api/analytics/visit").contentType(MediaType.APPLICATION_JSON).content(visit)
                .header("Sec-GPC", "1")).andExpect(status().isNoContent());
        mvc.perform(post("/api/analytics/visit").contentType(MediaType.APPLICATION_JSON).content("{\"path\":\"no-slash\"}"))
                .andExpect(status().isBadRequest());
        assertThat(visits.findAll()).hasSize(1);
        assertThat(visits.findAll().get(0).getIp()).isEqualTo("203.0.113.7");

        String token = login(PASSWORD, "10.0.0.2");
        mvc.perform(get("/api/admin/analytics/summary").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.pageViews").value(1))
                .andExpect(jsonPath("$.topReferrers[0].key").value("www.linkedin.com"));
        mvc.perform(get("/api/admin/analytics/visits").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$[0].ip").value("203.0.113.7"));
    }

    @Test
    void corsOnlyForTheSiteAndSecurityHeadersPresent() throws Exception {
        mvc.perform(options("/api/analytics/visit").header("Origin", "https://tiagorodrigues-gith.github.io")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://tiagorodrigues-gith.github.io"));
        mvc.perform(options("/api/analytics/visit").header("Origin", "https://evil.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/health"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }
}
