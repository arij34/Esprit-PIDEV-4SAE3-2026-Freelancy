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

  // ✅ CREATE avec userId
  create(userId: number, freelancerSkill: FreelancerSkill): Observable<FreelancerSkill> {
    return this.http.post<FreelancerSkill>(
      `${this.url}/user/${userId}`,
      freelancerSkill
    );
  }

  // ✅ UPDATE sans id dans URL
  update(freelancerSkill: FreelancerSkill): Observable<FreelancerSkill> {
    return this.http.put<FreelancerSkill>(
      this.url,
      freelancerSkill
    );
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  getLevelByYears(years: number): Observable<{years: number, level: number, label: string}> {
  return this.http.get<{years: number, level: number, label: string}>(
    `${this.url}/level/${years}`
  );
}


createWithSkillInput(userId: number, skillInput: string, payload: any): Observable<any> {
  return this.http.post<any>(
    `${this.url}/user/${userId}?skillInput=${encodeURIComponent(skillInput)}`,
    payload
  );
}

createWithSkillInputCV(userId: number, skillInput: string, payload: any): Observable<any> {
  return this.http.post<any>(
    `${this.url}/CV/${userId}?skillInput=${encodeURIComponent(skillInput)}`,
    payload
  );
}
 getDuplicateSkills(userId: number): Observable<FreelancerSkill[]> {
    return this.http.get<FreelancerSkill[]>(
      `${this.url}/${userId}/duplicate-skills`
    );
  }

  checkExistingSkills(userId: number, skills: string[]) {
  return this.http.post<any>(
    `${this.url}/check-existing/${userId}`,
    skills
  );
}
checkExistingSkillscv(userId: number, skills: string[]) {
  return this.http.post<any>(
    `${this.url}/check-skills/${userId}`,
    skills
  );
}

}
