import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ChallengeService } from './challenge.service';

describe('ChallengeService', () => {
  let service: ChallengeService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(ChallengeService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(service).toBeTruthy();
  });

  it('should map list response in getChallenges', () => {
    service.getChallenges().subscribe(challenges => {
      expect(challenges.length).toBe(1);
      expect(challenges[0].id).toBe('1');
      expect(challenges[0].title).toBe('Challenge A');
    });

    const req = httpMock.expectOne(r => r.url.includes('/challenges'));
    expect(req.request.method).toBe('GET');
    req.flush([{ id: 1, title: 'Challenge A', description: 'desc' }]);
  });

  it('should send filter params', () => {
    service.getChallenges({ difficulty: 'EASY', status: 'ACTIVE' }).subscribe();

    const req = httpMock.expectOne(r => r.url.includes('/challenges'));
    expect(req.request.params.get('difficulty')).toBe('EASY');
    expect(req.request.params.get('status')).toBe('ACTIVE');
    req.flush([]);
  });

  it('should update task status with patch query param', () => {
    service.updateTaskStatus('55', 'DONE').subscribe();

    const req = httpMock.expectOne(r => r.url.includes('/tasks/55/status'));
    expect(req.request.method).toBe('PATCH');
    expect(req.request.params.get('status')).toBe('DONE');
    req.flush({});
  });
});
