package com.social.analytics.repository;

import com.social.analytics.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByOrderByGeneratedAtDesc();
}
