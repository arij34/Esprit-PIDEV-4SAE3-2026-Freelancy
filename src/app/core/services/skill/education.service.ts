import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Education } from '../../models/skill/education.model';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class EducationService {
  private url = `${environment.apiUrl}/education`;
  constructor(private http: HttpClient) {}

  getAll(): Observable<Education[]> { 
    return this.http.get<Education[]>(this.url); 
  }
  
  getById(id: number): Observable<Education> { 
    return this.http.get<Education>(`${this.url}/${id}`); 
  }
  
  // ← POST /api/education/user/{userId}
  create(userId: number, e: Education): Observable<Education> { 
    return this.http.post<Education>(`${this.url}/user/${userId}`, e); 
  }
  
  // ← PUT /api/education (ID dans le body, pas dans l'URL)
  update(e: Education): Observable<Education> { 
    return this.http.put<Education>(this.url, e); 
  }
  
  delete(id: number): Observable<void> { 
    return this.http.delete<void>(`${this.url}/${id}`); 
  }
  getLatest(userId: number) {
  return this.http.get<Education>(
    `${this.url}/user/${userId}/latest`
  );
}
}