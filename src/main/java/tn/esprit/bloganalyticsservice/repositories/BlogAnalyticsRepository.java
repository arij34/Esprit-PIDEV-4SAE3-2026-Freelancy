package tn.esprit.bloganalyticsservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.bloganalyticsservice.entities.BlogAnalytics;

import java.util.Optional;

@Repository
public interface BlogAnalyticsRepository extends JpaRepository<BlogAnalytics, Long> {
    Optional<BlogAnalytics> findByMetric(String metric);
}