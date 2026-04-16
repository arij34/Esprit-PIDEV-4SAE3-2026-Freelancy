import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface BlogPost {
  idPost?: number;
  title: string;
  content: string;
  author: string;
  createdAt?: Date | string;
  updatedAt?: Date | string;
}

@Injectable({
  providedIn: 'root'
})
export class BlogService {
  private apiUrl = 'http://localhost:8050/posts';

  constructor(private http: HttpClient) {}

  getAllPosts(): Observable<BlogPost[]> {
    return this.http.get<BlogPost[]>(`${this.apiUrl}/all`);
  }

  addPost(post: BlogPost): Observable<BlogPost> {
    return this.http.post<BlogPost>(`${this.apiUrl}/add`, post);
  }

  updatePost(id: number, post: BlogPost): Observable<BlogPost> {
    return this.http.put<BlogPost>(`${this.apiUrl}/update/${id}`, post);
  }

  deletePost(id: number): Observable<string> {
    return this.http.delete<string>(`${this.apiUrl}/delete/${id}`);
  }
}