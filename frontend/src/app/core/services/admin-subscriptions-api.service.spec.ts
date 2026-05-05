import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AdminSubscriptionsApiService } from './admin-subscriptions-api.service';

describe('AdminSubscriptionsApiService', () => {
  let service: AdminSubscriptionsApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(AdminSubscriptionsApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(service).toBeTruthy();
  });

  it('should list all subscriptions', async () => {
    const p = service.listAll();

    const req = httpMock.expectOne('http://localhost:8091/api/admin/subscriptions');
    expect(req.request.method).toBe('GET');
    req.flush([{ id: 10 }]);

    const data = await p;
    expect(data.length).toBe(1);
  });

  it('should update status', async () => {
    const p = service.updateStatus(5, 'ACTIVE' as any);

    const req = httpMock.expectOne('http://localhost:8091/api/admin/subscriptions/5/status');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ status: 'ACTIVE' });
    req.flush({ id: 5, status: 'ACTIVE' });

    const data = await p;
    expect(data.status).toBe('ACTIVE');
  });

  it('should delete by id', async () => {
    const p = service.delete(8);

    const req = httpMock.expectOne('http://localhost:8091/api/admin/subscriptions/8');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);

    await expectAsync(p).toBeResolved();
  });
});
