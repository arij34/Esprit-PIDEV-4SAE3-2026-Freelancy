import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface BlogPost {
  idPost?: number;
  title: string;
  content: string;
  author: string;
  status?: 'PENDING' | 'APPROVED' | 'REJECTED';
  createdAt?: Date | string;
  updatedAt?: Date | string;
}

@Injectable({
  providedIn: 'root'
})
export class BlogService {
  private apiUrl = 'http://localhost:8050/posts';

  constructor(private http: HttpClient) {}

  private toPayload(post: BlogPost): Pick<BlogPost, 'title' | 'content' | 'author' | 'status'> {
    return {
      title: post.title.trim(),
      content: post.content.trim(),
      author: post.author.trim(),
      ...(post.status ? { status: post.status } : {})
    };
  }

  getAllPosts(): Observable<BlogPost[]> {
    return this.http.get<BlogPost[]>(`${this.apiUrl}/all`);
  }

  addPost(post: BlogPost): Observable<BlogPost> {
    return this.http.post<BlogPost>(`${this.apiUrl}/add`, this.toPayload(post));
  }

  updatePost(id: number, post: BlogPost): Observable<BlogPost> {
    return this.http.put<BlogPost>(`${this.apiUrl}/update/${id}`, this.toPayload(post));
  }

  deletePost(id: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/delete/${id}`, { responseType: 'text' });
  }
}
