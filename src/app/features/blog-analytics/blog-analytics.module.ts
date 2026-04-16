import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { BlogAnalyticsRoutingModule } from './blog-analytics-routing.module';
import { BlogAnalyticsPageComponent } from './pages/blog-analytics-page/blog-analytics-page.component';

@NgModule({
  declarations: [BlogAnalyticsPageComponent],
  imports: [
    CommonModule,
    FormsModule,
    BlogAnalyticsRoutingModule
  ]
})
export class BlogAnalyticsModule { }