package org.example.analytics;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
public class AnalyticsController {

    public record VisitRequest(@NotBlank @Size(max = 200) @Pattern(regexp = "^/[^\\s]*$") String path,
                               @Size(max = 5) String lang,
                               @Size(max = 300) String referrer,
                               @Size(max = 20) String screen,
                               @Size(max = 60) String timezone,
                               @Size(max = 36) String session) { }

    public record VisitRow(Instant at, String ip, String path, String lang, String referrer, String userAgent,
                           String screen, String timezone, String session) { }

    private final AnalyticsService analytics;

    public AnalyticsController(AnalyticsService analytics) {
        this.analytics = analytics;
    }

    /**
     * Public: records one page view. The client IP comes from the connection (behind a proxy, from
     * X-Forwarded-For via server.forward-headers-strategy), never from the request body.
     * Browsers sending Global Privacy Control or Do Not Track are not recorded.
     */
    @PostMapping("/api/analytics/visit")
    public ResponseEntity<Void> visit(@Valid @RequestBody VisitRequest body, HttpServletRequest http) {
        if ("1".equals(http.getHeader("Sec-GPC")) || "1".equals(http.getHeader("DNT"))) {
            return ResponseEntity.noContent().build();
        }
        String ua = http.getHeader("User-Agent");
        analytics.record(Visit.builder()
                .ip(http.getRemoteAddr())
                .userAgent(ua == null ? null : ua.substring(0, Math.min(ua.length(), 300)))
                .path(body.path())
                .lang(body.lang())
                .referrer(body.referrer())
                .screen(body.screen())
                .timezone(body.timezone())
                .session(body.session())
                .build());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/admin/analytics/summary")
    public AnalyticsService.Summary summary(@RequestParam(defaultValue = "30") int days) {
        return analytics.summary(Math.max(1, Math.min(days, 90)));
    }

    @GetMapping("/api/admin/analytics/visits")
    public List<VisitRow> visits(@RequestParam(defaultValue = "100") int limit) {
        return analytics.recent(Math.max(1, Math.min(limit, 500))).stream()
                .map(v -> new VisitRow(v.getAt(), v.getIp(), v.getPath(), v.getLang(), v.getReferrer(), v.getUserAgent(),
                        v.getScreen(), v.getTimezone(), v.getSession()))
                .toList();
    }
}
