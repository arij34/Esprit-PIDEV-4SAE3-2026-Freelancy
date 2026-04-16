package tn.esprit.bloganalyticsservice.services;

import org.springframework.stereotype.Service;
import tn.esprit.bloganalyticsservice.entities.BlogAnalytics;
import tn.esprit.bloganalyticsservice.dto.BlogStat;
import tn.esprit.bloganalyticsservice.repositories.BlogAnalyticsRepository;

import java.util.List;
import java.util.Objects;

@Service
public class BlogAnalyticsServiceImpl implements IBlogAnalyticsService {

    private final BlogAnalyticsRepository repo;

    public BlogAnalyticsServiceImpl(BlogAnalyticsRepository repo) {
        this.repo = repo;
    }

    @Override
    public BlogAnalytics addAnalytics(BlogAnalytics analytics) {
        return repo.save(analytics);
    }

    @Override
    public List<BlogAnalytics> addAllAnalytics(List<BlogAnalytics> analyticsList) {
        return repo.saveAll(analyticsList);
    }

    @Override
    public BlogAnalytics getAnalytics(Long id) {
        return repo.findById(id).orElse(null);
    }

    @Override
    public List<BlogAnalytics> getAllAnalytics() {
        return repo.findAll();
    }

    @Override
    public BlogAnalytics updateAnalytics(BlogAnalytics analytics) {
        return repo.save(analytics);
    }

    @Override
    public void deleteAnalytics(Long id) {
        repo.deleteById(id);
    }

    @Override
    public BlogAnalytics upsert(String metric, Long value) {
        BlogAnalytics a = repo.findByMetric(metric).orElse(new BlogAnalytics());
        a.setMetric(metric);
        a.setValue(value);
        return repo.save(a);
    }

    @Override
    public List<BlogAnalytics> getAll() {
        return repo.findAll();
    }

    @Override
    public BlogStat getStat(String metric) {
        BlogAnalytics a = repo.findByMetric(metric)
            .orElse(new BlogAnalytics(null, metric, 0L));

        return new BlogStat(
            a.getMetric(),
            Objects.requireNonNullElse(a.getValue(), 0L)
        );
    }
}