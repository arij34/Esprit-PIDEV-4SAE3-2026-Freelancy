import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface BlogAnalyticsMetric {
  idAnalytics?: number;
  metric: string;
  value: number;
}

@Injectable({
  providedIn: 'root'
})
export class BlogAnalyticsService {
  private readonly apiUrl = 'http://localhost:8053/analytics';

  constructor(private http: HttpClient) {}

  getAllAnalytics(): Observable<BlogAnalyticsMetric[]> {
    return this.http.get<BlogAnalyticsMetric[]>(`${this.apiUrl}/all`);
  }

  getStat(metric: string): Observable<BlogAnalyticsMetric> {
    return this.http.get<BlogAnalyticsMetric>(`${this.apiUrl}/stat/${encodeURIComponent(metric)}`);
  }

  upsertMetric(metric: string, value: number): Observable<BlogAnalyticsMetric> {
    return this.http.post<BlogAnalyticsMetric>(
      `${this.apiUrl}/upsert?metric=${encodeURIComponent(metric)}&value=${value}`,
      null
    );
  }
}
