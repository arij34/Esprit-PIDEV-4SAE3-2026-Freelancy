import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AdminStatsService } from './admin-stats.service';

describe('AdminStatsService', () => {
  let service: AdminStatsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(AdminStatsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(service).toBeTruthy();
  });

  it('should request users stats', async () => {
    const promise = service.users();

    const req = httpMock.expectOne('http://localhost:8090/api/admin/stats/users');
    expect(req.request.method).toBe('GET');
    req.flush({
      totalUsers: 1,
      clients: 1,
      freelancers: 0,
      admins: 0,
      activeUsers: 1,
      newUsersThisMonth: 1
    });

    const result = await promise;
    expect(result.totalUsers).toBe(1);
  });
});
