import { Component, OnInit } from '@angular/core';
import { BlogAnalyticsMetric, BlogAnalyticsService } from '../../services/blog-analytics.service';

@Component({
  selector: 'app-blog-analytics-page',
  templateUrl: './blog-analytics-page.component.html',
  styleUrls: ['./blog-analytics-page.component.css']
})
export class BlogAnalyticsPageComponent implements OnInit {
  metrics: BlogAnalyticsMetric[] = [];
  selectedStat: BlogAnalyticsMetric | null = null;

  upsertForm = {
    metric: '',
    value: 0
  };

  statMetric = '';

  loadingAll = false;
  loadingStat = false;
  submitting = false;

  successMessage = '';
  errorMessage = '';

  constructor(private analyticsService: BlogAnalyticsService) {}

  ngOnInit(): void {
    this.loadAllMetrics();
  }

  loadAllMetrics(): void {
    this.loadingAll = true;
    this.errorMessage = '';
    this.analyticsService.getAllAnalytics().subscribe({
      next: (data) => {
        this.metrics = Array.isArray(data) ? data : [];
        this.loadingAll = false;
      },
      error: (err) => {
        console.error('Error loading analytics', err);
        this.errorMessage = 'Failed to load analytics. Please verify the API Gateway and analytics service are running.';
        this.loadingAll = false;
      }
    });
  }

  saveMetric(): void {
    const metric = this.upsertForm.metric.trim();
    const value = Number(this.upsertForm.value);

    if (!metric) {
      this.errorMessage = 'Metric is required.';
      return;
    }

    if (!Number.isFinite(value) || value < 0) {
      this.errorMessage = 'Value must be a valid non-negative number.';
      return;
    }

    this.submitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.analyticsService.upsertMetric(metric, value).subscribe({
      next: () => {
        this.successMessage = `Metric "${metric}" saved successfully.`;
        this.upsertForm = { metric: '', value: 0 };
        this.submitting = false;
        this.loadAllMetrics();
      },
      error: (err) => {
        console.error('Error upserting metric', err);
        this.errorMessage = 'Failed to save metric.';
        this.submitting = false;
      }
    });
  }

  onFetchStat(): void {
    const metric = this.statMetric.trim();

    if (!metric) {
      this.errorMessage = 'Please enter a metric name.';
      return;
    }

    this.loadingStat = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.analyticsService.getStat(metric).subscribe({
      next: (data) => {
        this.selectedStat = data;
        this.successMessage = `Fetched stat for "${metric}".`;
        this.loadingStat = false;
      },
      error: (err) => {
        console.error('Error fetching stat', err);
        this.errorMessage = 'Failed to fetch metric stat.';
        this.loadingStat = false;
      }
    });
  }
}