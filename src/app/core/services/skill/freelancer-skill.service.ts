import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FreelancerSkill } from '../../models/skill/freelancer-skill.model';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class FreelancerSkillService {

  private baseUrl = `${environment.apiUrl}/freelancer-skill`;

  constructor(private http: HttpClient) {}

  // Récupère toutes les skills du user connecté
  getAll(): Observable<FreelancerSkill[]> {
    return this.http.get<FreelancerSkill[]>(`${this.baseUrl}/user/me`);
  }

  getById(id: number): Observable<FreelancerSkill> {
    return this.http.get<FreelancerSkill>(`${this.baseUrl}/${id}`);
  }

  // Création skill avec suggestion/force
  createWithSkillInput(skillInput: string, skill: FreelancerSkill, forceCreate = false): Observable<any> {
    return this.http.post<any>(
      `${this.baseUrl}/user/me?skillInput=${encodeURIComponent(skillInput)}&forceCreate=${forceCreate}`,
      skill
    );
  }

  // Création via CV (si besoin de l'utiliser)
  createWithSkillInputCV(skillInput: string, skill: FreelancerSkill): Observable<any> {
    return this.http.post<any>(
      `${this.baseUrl}/CV/me?skillInput=${encodeURIComponent(skillInput)}`,
      skill
    );
  }

  update(skill: FreelancerSkill): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}`, skill);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  // Doublons pour user connecté
  getDuplicateSkillsForCurrentUser(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/user/me/duplicates`);
  }

  // Vérifie existence de skills (pour pré-filtrer avant import CV)
  checkExistingSkillsForCurrentUser(skills: string[]): Observable<any> {
    return this.http.post<any>(
      `${this.baseUrl}/check-skills/me`,
      skills
    );
  }

  // Récupère le niveau par nombre d'années
  getLevelByYears(years: number): Observable<any> {
    // Attendu : /freelancer-skill/level/{years}
    return this.http.get<any>(`${this.baseUrl}/level/${years}`);
  }
}