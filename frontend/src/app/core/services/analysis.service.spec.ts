import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AnalysisService } from './analysis.service';

describe('AnalysisService', () => {
  let service: AnalysisService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(AnalysisService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(service).toBeTruthy();
  });

  it('should post analyzeProject payload', () => {
    service.analyzeProject('T', 'D', '2026-12-01').subscribe();

    const req = httpMock.expectOne('http://localhost:8085/analysis/analyze');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ title: 'T', description: 'D', deadline: '2026-12-01' });
    req.flush({});
  });

  it('should fetch saved project analysis', () => {
    service.getProjectAnalysis(7).subscribe();

    const req = httpMock.expectOne('http://localhost:8085/projects/7/analysis');
    expect(req.request.method).toBe('GET');
    req.flush({ feasibilityScore: 80 });
  });
});
