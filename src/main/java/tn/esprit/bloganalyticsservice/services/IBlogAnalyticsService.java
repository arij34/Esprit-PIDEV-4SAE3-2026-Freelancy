package tn.esprit.bloganalyticsservice.services;

import tn.esprit.bloganalyticsservice.entities.BlogAnalytics;
import tn.esprit.bloganalyticsservice.dto.BlogStat;

import java.util.List;

public interface IBlogAnalyticsService {
    BlogAnalytics addAnalytics(BlogAnalytics analytics);
    List<BlogAnalytics> addAllAnalytics(List<BlogAnalytics> analyticsList);

    BlogAnalytics getAnalytics(Long id);
    List<BlogAnalytics> getAllAnalytics();

    BlogAnalytics updateAnalytics(BlogAnalytics analytics);
    void deleteAnalytics(Long id);

    // Métier avancé
    BlogAnalytics upsert(String metric, Long value);
    List<BlogAnalytics> getAll();
    BlogStat getStat(String metric);
}