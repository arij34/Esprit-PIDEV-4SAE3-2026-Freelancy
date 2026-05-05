import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { FrontAccessGuard } from './front-access.guard';
import { AuthService } from './auth.service';

describe('FrontAccessGuard', () => {
  let guard: FrontAccessGuard;

  const routerMock = {
    parseUrl: jasmine.createSpy('parseUrl').and.returnValue({} as any)
  };

  const authMock = {
    isLoggedIn: jasmine.createSpy('isLoggedIn'),
    hasRole: jasmine.createSpy('hasRole')
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        FrontAccessGuard,
        { provide: Router, useValue: routerMock },
        { provide: AuthService, useValue: authMock }
      ]
    });
    guard = TestBed.inject(FrontAccessGuard);
  });

  it('should create', () => {
    expect(guard).toBeTruthy();
  });

  it('should allow non logged in users', async () => {
    authMock.isLoggedIn.and.resolveTo(false);

    const result = await guard.canActivate();
    expect(result).toBeTrue();
  });

  it('should redirect admin to dashboard', async () => {
    authMock.isLoggedIn.and.resolveTo(true);
    authMock.hasRole.and.returnValue(true);

    await guard.canActivate();
    expect(routerMock.parseUrl).toHaveBeenCalledWith('/admin/dashboard');
  });
});
