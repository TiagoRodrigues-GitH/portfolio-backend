package org.example.analytics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** One page view. Personal data (IP, user agent): deleted after app.analytics.retention-days. */
@Entity
@Table(name = "visits", indexes = @Index(name = "idx_visits_at", columnList = "at"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant at;

    @Column(nullable = false, length = 45)
    private String ip;

    @Column(name = "user_agent", length = 300)
    private String userAgent;

    @Column(nullable = false, length = 200)
    private String path;

    @Column(length = 5)
    private String lang;

    @Column(length = 300)
    private String referrer;

    @Column(length = 20)
    private String screen;

    @Column(length = 60)
    private String timezone;

    @Column(length = 36)
    private String session;
}
