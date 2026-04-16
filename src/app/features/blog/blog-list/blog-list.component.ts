import { Component, OnInit } from '@angular/core';
import { BlogService, BlogPost } from '../blog.service';

@Component({
  selector: 'app-blog-list',
  templateUrl: './blog-list.component.html',
  styleUrls: ['./blog-list.component.css']
})
export class BlogListComponent implements OnInit {
  posts: BlogPost[] = [];

  // Form state
  newPost: BlogPost = { title: '', content: '', author: '' };
  editPostId: number | null = null;
  editPost: BlogPost | null = null;

  // UI state
  loading = false;
  submitting = false;
  submitted = false;
  successMessage = '';
  errorMessage = '';

  constructor(private blogService: BlogService) {}

  ngOnInit(): void {
    this.loadPosts();
  }

  loadPosts(): void {
    this.loading = true;
    this.errorMessage = '';
    this.blogService.getAllPosts().subscribe({
      next: (data) => {
        this.posts = data || [];
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading posts', err);
        this.errorMessage = 'Failed to load posts. Please check if the blog service is running on port 8050.';
        this.loading = false;
      }
    });
  }

  isValid(): boolean {
    return this.newPost.title.trim().length > 0 &&
           this.newPost.content.trim().length > 0 &&
           this.newPost.author.trim().length > 0;
  }

  normalize(): void {
    this.newPost.title = this.newPost.title.trim();
    this.newPost.content = this.newPost.content.trim();
    this.newPost.author = this.newPost.author.trim();
  }

  onCreatePost(): void {
    if (!this.isValid()) {
      this.errorMessage = 'Please fill in all fields';
      return;
    }

    this.normalize();
    this.submitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.blogService.addPost(this.newPost).subscribe({
      next: (post) => {
        this.successMessage = 'Post created successfully!';
        this.newPost = { title: '', content: '', author: '' };
        this.submitted = true;
        this.submitting = false;
        setTimeout(() => {
          this.successMessage = '';
          this.submitted = false;
        }, 3000);
        this.loadPosts();
      },
      error: (err) => {
        console.error('Error creating post', err);
        this.errorMessage = 'Failed to create post. Please try again.';
        this.submitting = false;
      }
    });
  }

  startEdit(post: BlogPost): void {
    this.editPostId = post.idPost || null;
    this.editPost = { ...post };
  }

  cancelEdit(): void {
    this.editPostId = null;
    this.editPost = null;
    this.errorMessage = '';
  }

  onUpdatePost(): void {
    if (!this.editPost || !this.editPostId) return;

    if (this.editPost.title.trim().length === 0 ||
        this.editPost.content.trim().length === 0 ||
        this.editPost.author.trim().length === 0) {
      this.errorMessage = 'Please fill in all fields';
      return;
    }

    this.normalize();
    this.submitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.blogService.updatePost(this.editPostId, this.editPost).subscribe({
      next: () => {
        this.successMessage = 'Post updated successfully!';
        this.submitting = false;
        this.editPostId = null;
        this.editPost = null;
        setTimeout(() => {
          this.successMessage = '';
        }, 3000);
        this.loadPosts();
      },
      error: (err) => {
        console.error('Error updating post', err);
        this.errorMessage = 'Failed to update post. Please try again.';
        this.submitting = false;
      }
    });
  }

  deletePost(id: number | undefined): void {
    if (!id || !confirm('Are you sure you want to delete this post?')) return;

    this.blogService.deletePost(id).subscribe({
      next: () => {
        this.successMessage = 'Post deleted successfully!';
        setTimeout(() => {
          this.successMessage = '';
        }, 3000);
        this.loadPosts();
      },
      error: (err) => {
        console.error('Error deleting post', err);
        this.errorMessage = 'Failed to delete post. Please try again.';
      }
    });
  }

  formatDate(date: Date | string | undefined): string {
    if (!date) return '';
    return new Date(date).toLocaleDateString();
  }
}