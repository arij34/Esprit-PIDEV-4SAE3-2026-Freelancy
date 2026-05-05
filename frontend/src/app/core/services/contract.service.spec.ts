import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ContractService } from './contract.service';

describe('ContractService', () => {
  let service: ContractService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.setItem('access_token', 'token-abc');

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });

    service = TestBed.inject(ContractService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.removeItem('access_token');
  });

  it('should create', () => {
    expect(service).toBeTruthy();
  });

  it('should add Authorization header in getAllContracts', () => {
    service.getAllContracts().subscribe();

    const req = httpMock.expectOne('http://localhost:8087/api/contracts');
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe('Bearer token-abc');
    req.flush([]);
  });

  it('should increase unread notifications when pushing one', () => {
    const before = service.unreadCount;
    service.pushNotification({
      type: 'DRAFT_RECEIVED' as any,
      title: 'T',
      message: 'M',
      color: 'blue' as any,
      contractId: 1
    });
    expect(service.unreadCount).toBe(before + 1);
  });

  it('should mark all notifications as read', () => {
    service.markAllRead();
    expect(service.unreadCount).toBe(0);
  });
});
