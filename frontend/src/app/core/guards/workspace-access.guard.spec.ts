import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { WorkspaceAccessGuard } from './workspace-access.guard';

describe('WorkspaceAccessGuard', () => {
  let guard: WorkspaceAccessGuard;
  let httpMock: HttpTestingController;

  const routerMock = {
    navigate: jasmine.createSpy('navigate')
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        WorkspaceAccessGuard,
        { provide: Router, useValue: routerMock }
      ]
    });

    guard = TestBed.inject(WorkspaceAccessGuard);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should allow when backend says allowed', (done) => {
    const route: any = { params: { id: '42' }, queryParams: { userId: '11', role: 'CLIENT' } };

    guard.canActivate(route).subscribe(res => {
      expect(res).toBeTrue();
      done();
    });

    const req = httpMock.expectOne(r => r.url.includes('/projects/42/workspace/access'));
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('userId')).toBe('11');
    expect(req.request.params.get('role')).toBe('CLIENT');
    req.flush({ allowed: true, reason: '' });
  });

  it('should reject and navigate when backend denies', (done) => {
    const route: any = { params: { id: '42' }, queryParams: { userId: '11', role: 'CLIENT' } };

    guard.canActivate(route).subscribe(res => {
      expect(res).toBeFalse();
      expect(routerMock.navigate).toHaveBeenCalledWith(['/front-office/projects']);
      done();
    });

    const req = httpMock.expectOne(r => r.url.includes('/projects/42/workspace/access'));
    req.flush({ allowed: false, reason: 'forbidden' });
  });
});
