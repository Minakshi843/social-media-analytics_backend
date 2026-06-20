package com.social.analytics.repository;

import com.social.analytics.model.Analytics;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AnalyticsRepository extends JpaRepository<Analytics, Long> {
    List<Analytics> findByCityId(Long cityId);
    List<Analytics> findByDateBetween(LocalDate startDate, LocalDate endDate);
    List<Analytics> findByCityIdAndDateBetween(Long cityId, LocalDate startDate, LocalDate endDate);
    Optional<Analytics> findByCityIdAndDateAndPlatform(Long cityId, LocalDate date, String platform);
}
