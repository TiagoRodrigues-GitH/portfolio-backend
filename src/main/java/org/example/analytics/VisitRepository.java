package org.example.analytics;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    List<Visit> findByAtAfterOrderByAtDesc(Instant since, Pageable page);

    List<Visit> findByAtAfter(Instant since);

    long countByIpAndAtAfter(String ip, Instant since);

    @Modifying
    @Transactional
    @Query("delete from Visit v where v.at < :cutoff")
    int deleteOlderThan(Instant cutoff);
}
