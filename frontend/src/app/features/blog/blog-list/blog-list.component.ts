import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { BlogService, BlogPost } from '../blog.service';

@Component({
  selector: 'app-blog-list',
  templateUrl: './blog-list.component.html',
  styleUrls: ['./blog-list.component.css']
})
export class BlogListComponent implements OnInit {
  private readonly pinnedPostsStorageKey = 'blog-pinned-posts';
  private readonly likesStorageKey = 'blog-post-likes';
  private readonly dislikesStorageKey = 'blog-post-dislikes';
  private readonly badWords = ['badword', 'uglyword', 'stupid', 'idiot', 'hate'];

  posts: BlogPost[] = [];
  visiblePosts: BlogPost[] = [];

  searchTitle = '';
  selectedDate = '';
  isCalendarOpen = false;
  sortOrder: 'latest' | 'oldest' = 'latest';
  pageSize = 3;
  currentPage = 1;
  totalPages = 1;
  filteredPostsCount = 0;
  pinnedPostIds: number[] = [];
  postLikes: Record<number, number> = {};
  postDislikes: Record<number, number> = {};
  likesRanking: Array<{ postId: number; title: string; likes: number; dislikes: number }> = [];

  newPost: BlogPost = { title: '', content: '', author: '' };
  editPostId: number | null = null;
  editPost: BlogPost | null = null;

  loading = false;
  submitting = false;
  submitted = false;
  successMessage = '';
  errorMessage = '';

  constructor(
    private blogService: BlogService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadPinnedPostIds();
    this.loadPostLikes();
    this.loadPostDislikes();
    this.loadPosts();
  }

  loadPosts(): void {
    this.loading = true;
    this.errorMessage = '';
    this.blogService.getAllPosts().subscribe({
      next: (data) => {
        this.posts = data || [];
        this.cleanupVoteDataForExistingPosts();
        this.rebuildLikesRanking();
        this.currentPage = 1;
        this.refreshDisplayedPosts();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading posts', err);
        this.errorMessage = 'Failed to load posts. Please check if the Blog Service is running on port 8050.';
        this.posts = [];
        this.visiblePosts = [];
        this.likesRanking = [];
        this.filteredPostsCount = 0;
        this.totalPages = 1;
        this.currentPage = 1;
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

  private normalizePost(post: BlogPost): void {
    post.title = post.title.trim();
    post.content = post.content.trim();
    post.author = post.author.trim();
  }

  private findBadWord(post: BlogPost): string | null {
    const text = `${post.title} ${post.content} ${post.author}`.toLowerCase();

    return this.badWords.find((word) => {
      const escaped = word.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
      return new RegExp(`\\b${escaped}\\b`, 'i').test(text);
    }) || null;
  }

  onCreatePost(): void {
    if (!this.isValid()) {
      this.errorMessage = 'Please fill in all fields';
      return;
    }

    const badWord = this.findBadWord(this.newPost);
    if (badWord) {
      this.errorMessage = `Please remove inappropriate word: "${badWord}".`;
      return;
    }

    this.normalize();
    this.submitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.blogService.addPost(this.newPost).subscribe({
      next: () => {
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

    const badWord = this.findBadWord(this.editPost);
    if (badWord) {
      this.errorMessage = `Please remove inappropriate word: "${badWord}".`;
      return;
    }

    this.normalizePost(this.editPost);
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
        this.unpinPost(id);
        this.removePostVoteData(id);
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

  onSearchInput(): void {
    this.currentPage = 1;
    this.refreshDisplayedPosts();
  }

  onSearchClick(): void {
    this.currentPage = 1;
    this.refreshDisplayedPosts();
  }

  toggleCalendar(): void {
    this.isCalendarOpen = !this.isCalendarOpen;
  }

  onDateFilterChange(value: string): void {
    this.selectedDate = value;
    this.isCalendarOpen = false;
    this.currentPage = 1;
    this.refreshDisplayedPosts();
  }

  clearDateFilter(): void {
    this.selectedDate = '';
    this.currentPage = 1;
    this.refreshDisplayedPosts();
  }

  toggleSortOrder(): void {
    this.sortOrder = this.sortOrder === 'latest' ? 'oldest' : 'latest';
    this.currentPage = 1;
    this.refreshDisplayedPosts();
  }

  goToPreviousPage(): void {
    if (this.currentPage <= 1) return;
    this.currentPage -= 1;
    this.refreshDisplayedPosts();
  }

  goToNextPage(): void {
    if (this.currentPage >= this.totalPages) return;
    this.currentPage += 1;
    this.refreshDisplayedPosts();
  }

  togglePinPost(post: BlogPost): void {
    if (!post.idPost) return;

    if (this.isPinned(post.idPost)) {
      this.unpinPost(post.idPost);
      this.successMessage = `Unpinned "${post.title}".`;
    } else {
      this.pinPost(post.idPost);
      this.successMessage = `Pinned "${post.title}" to the top.`;
    }

    setTimeout(() => {
      this.successMessage = '';
    }, 2500);

    this.refreshDisplayedPosts();
  }

  incrementLikes(post: BlogPost): void {
    if (!post.idPost) return;
    this.adjustLikes(post.idPost, 1);
    this.savePostLikes();
    this.rebuildLikesRanking();
  }

  decrementLikes(post: BlogPost): void {
    if (!post.idPost) return;

    this.adjustLikes(post.idPost, -1);
    this.savePostLikes();
    this.rebuildLikesRanking();
  }

  incrementDislikes(post: BlogPost): void {
    if (!post.idPost) return;

    this.adjustDislikes(post.idPost, 1);
    this.savePostDislikes();
    this.rebuildLikesRanking();
  }

  decrementDislikes(post: BlogPost): void {
    if (!post.idPost) return;

    this.adjustDislikes(post.idPost, -1);
    this.savePostDislikes();
    this.rebuildLikesRanking();
  }

  getLikes(postId: number | undefined): number {
    if (!postId) return 0;
    return this.postLikes[postId] ?? 0;
  }

  getDislikes(postId: number | undefined): number {
    if (!postId) return 0;
    return this.postDislikes[postId] ?? 0;
  }

  isPinned(postId: number | undefined): boolean {
    return !!postId && this.pinnedPostIds.includes(postId);
  }

  formatDate(date: Date | string | undefined): string {
    if (!date) return '';
    return new Date(date).toLocaleString([], {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getHomeRoute(): string {
    return this.router.url.startsWith('/admin') ? '/admin/dashboard' : '/front';
  }

  getAnalyticsRoute(): string {
    return this.router.url.startsWith('/admin') ? '/admin/blog-analytics' : '/front/blog-analytics';
  }

  private getPostTimestamp(post: BlogPost): number {
    const sourceDate = post.updatedAt ?? post.createdAt;
    if (!sourceDate) return 0;
    const timestamp = new Date(sourceDate).getTime();
    return Number.isFinite(timestamp) ? timestamp : 0;
  }

  private getPostDateKey(post: BlogPost): string {
    const sourceDate = post.updatedAt ?? post.createdAt;
    if (!sourceDate) return '';

    const parsed = new Date(sourceDate);
    if (!Number.isFinite(parsed.getTime())) return '';

    const year = parsed.getFullYear();
    const month = String(parsed.getMonth() + 1).padStart(2, '0');
    const day = String(parsed.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private loadPinnedPostIds(): void {
    try {
      const stored = localStorage.getItem(this.pinnedPostsStorageKey);
      this.pinnedPostIds = stored ? JSON.parse(stored) : [];
    } catch {
      this.pinnedPostIds = [];
    }
  }

  private savePinnedPostIds(): void {
    localStorage.setItem(this.pinnedPostsStorageKey, JSON.stringify(this.pinnedPostIds));
  }

  private loadPostLikes(): void {
    try {
      const stored = localStorage.getItem(this.likesStorageKey);
      this.postLikes = stored ? JSON.parse(stored) : {};
    } catch {
      this.postLikes = {};
    }
  }

  private savePostLikes(): void {
    localStorage.setItem(this.likesStorageKey, JSON.stringify(this.postLikes));
  }

  private loadPostDislikes(): void {
    try {
      const stored = localStorage.getItem(this.dislikesStorageKey);
      this.postDislikes = stored ? JSON.parse(stored) : {};
    } catch {
      this.postDislikes = {};
    }
  }

  private savePostDislikes(): void {
    localStorage.setItem(this.dislikesStorageKey, JSON.stringify(this.postDislikes));
  }

  private adjustLikes(postId: number, delta: number): void {
    const current = this.postLikes[postId] ?? 0;
    this.postLikes[postId] = Math.max(0, current + delta);
  }

  private adjustDislikes(postId: number, delta: number): void {
    const current = this.postDislikes[postId] ?? 0;
    this.postDislikes[postId] = Math.max(0, current + delta);
  }

  private pinPost(postId: number): void {
    if (this.pinnedPostIds.includes(postId)) return;
    this.pinnedPostIds = [postId, ...this.pinnedPostIds];
    this.savePinnedPostIds();
  }

  private unpinPost(postId: number): void {
    this.pinnedPostIds = this.pinnedPostIds.filter((id) => id !== postId);
    this.savePinnedPostIds();
  }

  private removePostVoteData(postId: number): void {
    delete this.postLikes[postId];
    delete this.postDislikes[postId];
    this.savePostLikes();
    this.savePostDislikes();
    this.rebuildLikesRanking();
  }

  private cleanupVoteDataForExistingPosts(): void {
    const existingIds = new Set(
      this.posts
        .map((post) => post.idPost)
        .filter((id): id is number => typeof id === 'number')
    );

    const cleanedLikes: Record<number, number> = {};
    Object.entries(this.postLikes).forEach(([idStr, likes]) => {
      const id = Number(idStr);
      if (existingIds.has(id)) {
        cleanedLikes[id] = likes;
      }
    });

    const cleanedDislikes: Record<number, number> = {};
    Object.entries(this.postDislikes).forEach(([idStr, dislikes]) => {
      const id = Number(idStr);
      if (existingIds.has(id)) {
        cleanedDislikes[id] = dislikes;
      }
    });

    this.postLikes = cleanedLikes;
    this.postDislikes = cleanedDislikes;
    this.savePostLikes();
    this.savePostDislikes();
  }

  private rebuildLikesRanking(): void {
    this.likesRanking = this.posts
      .filter((post): post is BlogPost & { idPost: number } => typeof post.idPost === 'number')
      .map((post) => ({
        postId: post.idPost,
        title: post.title,
        likes: this.getLikes(post.idPost),
        dislikes: this.getDislikes(post.idPost)
      }))
      .sort((a, b) => {
        if (b.likes !== a.likes) {
          return b.likes - a.likes;
        }
        return a.title.localeCompare(b.title);
      });
  }

  private refreshDisplayedPosts(): void {
    const query = this.searchTitle.trim().toLowerCase();

    const filtered = this.posts.filter((post) => {
      const titleMatch = !query || post.title.toLowerCase().includes(query);
      const dateMatch = !this.selectedDate || this.getPostDateKey(post) === this.selectedDate;
      return titleMatch && dateMatch;
    });

    const sorted = [...filtered].sort((a, b) => {
      const pinnedA = this.isPinned(a.idPost) ? 1 : 0;
      const pinnedB = this.isPinned(b.idPost) ? 1 : 0;

      if (pinnedA !== pinnedB) {
        return pinnedB - pinnedA;
      }

      const timeA = this.getPostTimestamp(a);
      const timeB = this.getPostTimestamp(b);
      return this.sortOrder === 'latest' ? timeB - timeA : timeA - timeB;
    });

    this.filteredPostsCount = sorted.length;
    this.totalPages = Math.max(1, Math.ceil(this.filteredPostsCount / this.pageSize));

    if (this.currentPage > this.totalPages) {
      this.currentPage = this.totalPages;
    }
    if (this.currentPage < 1) {
      this.currentPage = 1;
    }

    const start = (this.currentPage - 1) * this.pageSize;
    this.visiblePosts = sorted.slice(start, start + this.pageSize);
  }
}
