package tn.esprit.bloganalyticsservice.dto;

public class BlogStat {

    private String metric;
    private Long value;

    public BlogStat() {
    }

    public BlogStat(String metric, Long value) {
        this.metric = metric;
        this.value = value;
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