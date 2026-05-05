import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { BlogAnalyticsRoutingModule } from './blog-analytics-routing.module';
import { BlogAnalyticsPageComponent } from './pages/blog-analytics-page/blog-analytics-page.component';

@NgModule({
  imports: [
    CommonModule,
    BlogAnalyticsRoutingModule,
    BlogAnalyticsPageComponent
  ]
})
export class BlogAnalyticsModule { }
