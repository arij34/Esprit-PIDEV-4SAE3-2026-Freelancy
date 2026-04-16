package tn.esprit.bloganalyticsservice.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class BlogAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAnalytics;

    private String metric;

    private Long value;

    public BlogAnalytics() {
    }

    public BlogAnalytics(Long idAnalytics, String metric, Long value) {
        this.idAnalytics = idAnalytics;
        this.metric = metric;
        this.value = value;
    }

    public Long getIdAnalytics() {
        return idAnalytics;
    }

    public void setIdAnalytics(Long idAnalytics) {
        this.idAnalytics = idAnalytics;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
}