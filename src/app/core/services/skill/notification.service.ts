import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export interface AppNotification {
  id: number;
  type: 'PENDING_SKILL_ADDED' | 'SKILL_APPROVED' | 'SKILL_REJECTED';
  message: string;
  skillName: string;
  freelancerId: number;
  freelancerName: string;
  read: boolean;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {

  private apiUrl  = `${environment.apiUrl}/notifications`;
  // ✅ CORRIGÉ : WebSocket pointe directement vers le Gateway :8081
  private wsUrl   = `${environment.wsUrl}/ws-notifications`;

  private stompClient!: Client;
  private notificationsSubject = new BehaviorSubject<AppNotification[]>([]);
  private unreadCountSubject   = new BehaviorSubject<number>(0);

  notifications$ = this.notificationsSubject.asObservable();
  unreadCount$   = this.unreadCountSubject.asObservable();

  constructor(private http: HttpClient) {}

  // ══════════════════════════════════════════════════════════════
  // WEBSOCKET
  // ══════════════════════════════════════════════════════════════

  connect(role: 'ADMIN' | 'USER', userId?: number): void {
    this.stompClient = new Client({
      webSocketFactory: () => new (SockJS as any)(this.wsUrl),
      reconnectDelay: 5000,
      onConnect: () => {
        console.log('✅ WebSocket connecté');

        const topic = role === 'ADMIN'
          ? '/topic/admin-notifications'
          : `/topic/user-notifications/${userId}`;

        this.stompClient.subscribe(topic, (msg: IMessage) => {
          const notif: AppNotification = JSON.parse(msg.body);
          this.onNewNotification(notif);
        });

        this.loadNotifications(role);
      }
    });

    this.stompClient.activate();
  }

  disconnect(): void {
    if (this.stompClient?.active) {
      this.stompClient.deactivate();
    }
  }

  // ══════════════════════════════════════════════════════════════
  // INTERNE
  // ══════════════════════════════════════════════════════════════

  private onNewNotification(notif: AppNotification): void {
    const current = this.notificationsSubject.getValue();
    this.notificationsSubject.next([notif, ...current]);
    if (!notif.read) {
      this.unreadCountSubject.next(this.unreadCountSubject.getValue() + 1);
    }
  }

  private loadNotifications(role: 'ADMIN' | 'USER'): void {
    // ✅ /user/me — token Keycloak injecté automatiquement
    const url = role === 'ADMIN'
      ? `${this.apiUrl}/admin`
      : `${this.apiUrl}/user/me`;

    this.http.get<AppNotification[]>(url).subscribe({
      next: (data) => {
        this.notificationsSubject.next(data);
        this.unreadCountSubject.next(data.filter(n => !n.read).length);
      },
      error: (err) => console.error('Failed to load notifications', err)
    });
  }

  // ══════════════════════════════════════════════════════════════
  // API REST
  // ══════════════════════════════════════════════════════════════

  getAdminNotifications(): Observable<AppNotification[]> {
    return this.http.get<AppNotification[]>(`${this.apiUrl}/admin`);
  }

  getAdminUnreadCount(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.apiUrl}/admin/unread-count`);
  }

  // ✅ /user/me
  getUserNotificationsForCurrentUser(): Observable<AppNotification[]> {
    return this.http.get<AppNotification[]>(`${this.apiUrl}/user/me`);
  }

  getUserUnreadCountForCurrentUser(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.apiUrl}/user/me/unread-count`);
  }

  markAllReadAdmin(): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/admin/mark-all-read`, {});
  }

  markAllReadCurrentUser(): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/user/me/mark-all-read`, {});
  }

  markOneRead(id: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/read`, {});
  }

  markAllReadLocally(): void {
    const updated = this.notificationsSubject.getValue().map(n => ({ ...n, read: true }));
    this.notificationsSubject.next(updated);
    this.unreadCountSubject.next(0);
  }
}
