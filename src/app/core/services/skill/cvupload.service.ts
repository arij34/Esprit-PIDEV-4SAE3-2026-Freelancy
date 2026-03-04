import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CvuploadService {

  constructor(private http: HttpClient) {}

  uploadCV(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<any>(
      'http://localhost:8086/api/cv/upload',
      formData
    );
  }
}