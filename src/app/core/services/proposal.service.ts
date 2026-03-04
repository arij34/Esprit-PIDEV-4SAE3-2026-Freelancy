import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ProposalService {

  private api = 'http://localhost:8085/proposals';

  constructor(private http: HttpClient) {}

  submit(data: any): Observable<any> {
    return this.http.post(this.api, data);
  }

  // ← clientId optionnel ajouté pour que le backend vérifie l'accès correctement
  getByProject(projectId: number, clientId?: string): Observable<any[]> {
    let params = new HttpParams();
    if (clientId) params = params.set('clientId', clientId);
    return this.http.get<any[]>(`${this.api}/project/${projectId}`, { params });
  }

  getByFreelancer(freelancerId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}/freelancer/${freelancerId}`);
  }

  getByFreelancerKeycloak(keycloakId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}/freelancer/keycloak/${keycloakId}`);
  }

  check(projectId: number, freelancerId: number): Observable<any> {
    const params = new HttpParams()
      .set('projectId',    projectId.toString())
      .set('freelancerId', freelancerId.toString());
    return this.http.get(`${this.api}/check`, { params });
  }

  checkByKeycloak(projectId: number, keycloakId: string): Observable<any> {
    const params = new HttpParams()
      .set('projectId',  projectId.toString())
      .set('keycloakId', keycloakId);
    return this.http.get(`${this.api}/check-by-keycloak`, { params });
  }

  countProposals(projectId: number): Observable<any> {
    return this.http.get(`${this.api}/count/${projectId}`);
  }

  /**
   * Updates proposal status (ACCEPTED/REJECTED).
   * Backend fetches freelancer from User Service and sends acceptance/rejection email.
   * Requires Authorization header (Bearer token) – added by KeycloakBearerInterceptor.
   */
  updateStatus(id: number, status: string): Observable<any> {
    return this.http.put(`${this.api}/${id}/status`, { status });
  }

  withdraw(id: number): Observable<any> {
    return this.http.delete(`${this.api}/${id}`);
  }
}