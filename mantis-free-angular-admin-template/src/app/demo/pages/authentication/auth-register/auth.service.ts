import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from './user.model';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private apiUrl = 'http://localhost:8080/api/auth';

    constructor(private http: HttpClient) { }

    register(user: User): Observable<any> {
        return this.http.post(`${this.apiUrl}/register`, user);
    }

    login(credentials: { email: string, password: string }): Observable<any> {
        return this.http.post(`${this.apiUrl}/login`, credentials);
    }

    updateRole(id: number, role: string): Observable<any> {
        return this.http.put(`http://localhost:8080/api/users/${id}/role`, { role });
    }
}
