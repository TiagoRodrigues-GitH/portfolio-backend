package org.example.analytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private static final Logger LOG = LoggerFactory.getLogger(AnalyticsService.class);
    /** Page views accepted per IP address per minute (anything above is dropped silently). */
    static final int PER_MINUTE_LIMIT = 30;

    public record CountRow(String key, long count) { }

    public record Summary(int days, long pageViews, long visitors, long sessions, List<CountRow> topPages,
                          List<CountRow> topReferrers, List<CountRow> languages, List<CountRow> perDay) { }

    private final VisitRepository visits;
    private final Duration retention;
    private final Clock clock;

    public AnalyticsService(VisitRepository visits, @Value("${app.analytics.retention-days:90}") int retentionDays) {
        this.visits = visits;
        this.retention = Duration.ofDays(retentionDays);
        this.clock = Clock.systemUTC();
    }

    /** Returns false when the visit was not stored (rate limit). */
    public boolean record(Visit visit) {
        Instant now = clock.instant();
        if (visits.countByIpAndAtAfter(visit.getIp(), now.minusSeconds(60)) >= PER_MINUTE_LIMIT) {
            return false;
        }
        visit.setAt(now);
        visits.save(visit);
        return true;
    }

    public List<Visit> recent(int limit) {
        return visits.findByAtAfterOrderByAtDesc(clock.instant().minus(retention), PageRequest.of(0, limit));
    }

    public Summary summary(int days) {
        List<Visit> rows = visits.findByAtAfter(clock.instant().minus(Duration.ofDays(days)));
        return new Summary(
                days,
                rows.size(),
                rows.stream().map(Visit::getIp).distinct().count(),
                rows.stream().map(Visit::getSession).filter(Objects::nonNull).filter(s -> !s.isEmpty()).distinct().count(),
                top(rows, Visit::getPath, 10),
                top(rows.stream().filter(v -> v.getReferrer() != null).toList(), v -> host(v.getReferrer()), 10),
                top(rows, Visit::getLang, 5),
                rows.stream().collect(Collectors.groupingBy(v -> v.getAt().atZone(ZoneOffset.UTC).toLocalDate().toString(), Collectors.counting()))
                        .entrySet().stream().sorted(Map.Entry.comparingByKey())
                        .map(e -> new CountRow(e.getKey(), e.getValue())).toList());
    }

    /** LGPD retention: visit records (IP, user agent) are deleted after the retention period. */
    @Scheduled(cron = "0 30 3 * * *", zone = "UTC")
    public void purgeOld() {
        int removed = visits.deleteOlderThan(clock.instant().minus(retention));
        if (removed > 0) {
            LOG.info("Deleted {} visit records older than {} days", removed, retention.toDays());
        }
    }

    private static List<CountRow> top(List<Visit> rows, Function<Visit, String> key, int n) {
        return rows.stream().map(key).filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()).thenComparing(Map.Entry.comparingByKey()))
                .limit(n).map(e -> new CountRow(e.getKey(), e.getValue())).toList();
    }

    private static String host(String url) {
        try {
            String h = URI.create(url).getHost();
            return h != null ? h : url;
        } catch (IllegalArgumentException e) {
            return url;
        }
    }
}
