import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { BlogListComponent } from './blog-list.component';
import { BlogService } from '../blog.service';

describe('BlogListComponent', () => {
  let component: BlogListComponent;
  let fixture: ComponentFixture<BlogListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [BlogListComponent],
      imports: [FormsModule, RouterTestingModule],
      providers: [
        {
          provide: BlogService,
          useValue: {
            getAllPosts: () => of([]),
            addPost: () => of({}),
            updatePost: () => of({}),
            deletePost: () => of('ok')
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(BlogListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
