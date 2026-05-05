import { TestBed } from '@angular/core/testing';
import { KeycloakService } from 'keycloak-angular';
import { AuthService } from './auth.service';
import { KC_ROLES } from './roles';

describe('AuthService', () => {
  let service: AuthService;

  const keycloakMock: any = {
    isLoggedIn: jasmine.createSpy('isLoggedIn').and.resolveTo(true),
    getUsername: jasmine.createSpy('getUsername').and.returnValue('john'),
    getToken: jasmine.createSpy('getToken').and.resolveTo('token-123'),
    updateToken: jasmine.createSpy('updateToken').and.resolveTo(true),
    getUserRoles: jasmine.createSpy('getUserRoles').and.returnValue([KC_ROLES.ADMIN]),
    getKeycloakInstance: jasmine.createSpy('getKeycloakInstance').and.returnValue({
      tokenParsed: { name: 'John Doe', sub: 'abc-123' },
      idToken: 'id-token-1',
      logout: jasmine.createSpy('logout').and.resolveTo(undefined)
    }),
    login: jasmine.createSpy('login').and.resolveTo(undefined)
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [{ provide: KeycloakService, useValue: keycloakMock }]
    });
    service = TestBed.inject(AuthService);
  });

  it('should create', () => {
    expect(service).toBeTruthy();
  });

  it('should route ADMIN to admin dashboard', () => {
    keycloakMock.getUserRoles.and.returnValue([KC_ROLES.ADMIN]);
    expect(service.getDefaultRouteByRole()).toBe('/admin/dashboard');
  });

  it('should route CLIENT to front', () => {
    keycloakMock.getUserRoles.and.returnValue([KC_ROLES.CLIENT]);
    expect(service.getDefaultRouteByRole()).toBe('/front');
  });

  it('should expose display name from token', () => {
    expect(service.getDisplayName()).toBe('John Doe');
  });

  it('should return keycloak subject', () => {
    expect(service.getKeycloakSub()).toBe('abc-123');
  });
});
