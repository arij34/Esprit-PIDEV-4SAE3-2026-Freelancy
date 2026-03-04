import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Observable, map, catchError, of } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class WorkspaceAccessGuard implements CanActivate {

  // ⚠️ Temporaire — sera remplacé par JWT
  private currentUserId   = 1;
  private currentUserRole = 'CLIENT'; // 'CLIENT' ou 'FREELANCER'

  constructor(private http: HttpClient, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot): Observable<boolean> {
    const projectId = +route.params['id'];

    return this.http.get<{ allowed: boolean; reason: string }>(
      `http://localhost:8085/projects/${projectId}/workspace/access`,
      { params: {
          userId: this.currentUserId.toString(),
          role:   this.currentUserRole
      }}
    ).pipe(
      map(res => {
        if (res.allowed) return true;
        this.router.navigate(['/front-office/projects']);
        return false;
      }),
      catchError(() => {
        this.router.navigate(['/front-office/projects']);
        return of(false);
      })
    );
  }
}