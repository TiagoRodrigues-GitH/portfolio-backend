package org.example.auth;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Brute-force protection for the login: after 5 failures from one address within
 * 15 minutes, further attempts from it are refused until the window ends.
 * In memory (one instance); enough for a single-admin portfolio.
 */
@Component
public class LoginAttempts {

    static final int MAX_FAILURES = 5;
    static final Duration WINDOW = Duration.ofMinutes(15);

    private record Entry(int failures, Instant firstFailure) { }

    private final Map<String, Entry> failures = new ConcurrentHashMap<>();
    private final Clock clock;

    public LoginAttempts() {
        this(Clock.systemUTC());
    }

    LoginAttempts(Clock clock) {
        this.clock = clock;
    }

    public boolean isBlocked(String client) {
        Entry e = failures.get(client);
        if (e == null) {
            return false;
        }
        if (e.firstFailure().plus(WINDOW).isBefore(clock.instant())) {
            failures.remove(client);
            return false;
        }
        return e.failures() >= MAX_FAILURES;
    }

    public void failed(String client) {
        Instant now = clock.instant();
        failures.compute(client, (k, e) -> e == null || e.firstFailure().plus(WINDOW).isBefore(now)
                ? new Entry(1, now) : new Entry(e.failures() + 1, e.firstFailure()));
        if (failures.size() > 10_000) {   // bound memory under a distributed attack
            failures.entrySet().removeIf(en -> en.getValue().firstFailure().plus(WINDOW).isBefore(now));
        }
    }

    public void succeeded(String client) {
        failures.remove(client);
    }
}
