import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FreelancerSkill } from '../../models/skill/freelancer-skill.model';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class FreelancerSkillService {

  private url = `${environment.apiUrl}/freelancer-skill`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<FreelancerSkill[]> {
    return this.http.get<FreelancerSkill[]>(this.url);
  }

  getById(id: number): Observable<FreelancerSkill> {
    return this.http.get<FreelancerSkill>(`${this.url}/${id}`);
  }

  // ✅ Token Keycloak injecté automatiquement — /user/me
  getAllForCurrentUser(): Observable<FreelancerSkill[]> {
    return this.http.get<FreelancerSkill[]>(`${this.url}/user/me`);
  }

  // ✅ Création manuelle — /user/me?skillInput=...
  createWithSkillInput(skillInput: string, payload: FreelancerSkill): Observable<any> {
    return this.http.post<any>(
      `${this.url}/user/me?skillInput=${encodeURIComponent(skillInput)}`,
      payload
    );
  }

  // ✅ Création depuis CV — /CV/me?skillInput=...
  createWithSkillInputCV(skillInput: string, payload: FreelancerSkill): Observable<any> {
    return this.http.post<any>(
      `${this.url}/CV/me?skillInput=${encodeURIComponent(skillInput)}`,
      payload
    );
  }

  update(freelancerSkill: FreelancerSkill): Observable<any> {
    return this.http.put<any>(this.url, freelancerSkill);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  getLevelByYears(years: number): Observable<{ years: number; level: number; label: string }> {
    return this.http.get<{ years: number; level: number; label: string }>(
      `${this.url}/level/${years}`
    );
  }

  getDuplicateSkills(id: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.url}/${id}/duplicate-skills`);
  }

  // ✅ /check-skills/me — token Keycloak injecté automatiquement
  checkSkillsForCurrentUser(skills: string[]): Observable<any> {
    return this.http.post<any>(`${this.url}/check-skills/me`, skills);
  }

  // ✅ /check-existing/me
  checkExistingSkillsForCurrentUser(skills: string[]): Observable<any> {
    return this.http.post<any>(`${this.url}/check-existing/me`, skills);
  }
}
