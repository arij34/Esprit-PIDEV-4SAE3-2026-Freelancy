import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class EventService {
private apiUrl = 'http://localhost:8084/evenment/api/events';
  constructor(private http: HttpClient) {}

  // ➕ Create
  addEvent(event: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, event);
  }

  // 📥 Read All
  getAllEvents(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  // 📥 Read By Id
  getEventById(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  // ✏ Update
  updateEvent(id: number, event: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, event);
  }

  // ❌ Delete
  deleteEvent(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}