import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { BlogRoutingModule } from './blog-routing.module';
import { BlogComponent } from './blog.component';
import { BlogListComponent } from './blog-list/blog-list.component';


@NgModule({
  declarations: [
    BlogComponent,
    BlogListComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    BlogRoutingModule
  ]
})
export class BlogModule { }
