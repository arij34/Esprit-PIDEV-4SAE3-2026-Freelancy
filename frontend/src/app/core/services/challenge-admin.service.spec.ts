import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ChallengeAdminService } from './challenge-admin.service';
import { ChallengeService } from './challenge.service';
import { ParticipationService } from './participation.service';

describe('ChallengeAdminService', () => {
  let service: ChallengeAdminService;

  const challengeServiceMock = {
    getChallenges: jasmine.createSpy('getChallenges').and.returnValue(of([])),
    getChallengeById: jasmine.createSpy('getChallengeById').and.returnValue(of({ id: '1' })),
    getTasksByChallengeId: jasmine.createSpy('getTasksByChallengeId').and.returnValue(of([])),
    updateChallenge: jasmine.createSpy('updateChallenge').and.returnValue(of({ id: '1' })),
    deleteChallenge: jasmine.createSpy('deleteChallenge').and.returnValue(of(void 0)),
    updateTask: jasmine.createSpy('updateTask').and.returnValue(of({})),
    deleteTask: jasmine.createSpy('deleteTask').and.returnValue(of(void 0)),
    addChallenge: jasmine.createSpy('addChallenge').and.returnValue(of({ id: 'new' }))
  };

  const participationServiceMock = {
    getParticipationsByChallenge: jasmine.createSpy('getParticipationsByChallenge').and.returnValue(of([
      { id: 'p1', usernameGithub: 'userx', status: 'COMPLETED', forkCreatedAt: '2026-01-01T00:00:00Z' }
    ]))
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ChallengeAdminService,
        { provide: ChallengeService, useValue: challengeServiceMock },
        { provide: ParticipationService, useValue: participationServiceMock }
      ]
    });

    service = TestBed.inject(ChallengeAdminService);
  });

  it('should create', () => {
    expect(service).toBeTruthy();
  });

  it('should map participation status to Completed participant', (done) => {
    service.getParticipants('1').subscribe(res => {
      expect(res.length).toBe(1);
      expect(res[0].status).toBe('Completed');
      done();
    });
  });

  it('should duplicate challenge with copy title', () => {
    const challenge: any = {
      id: '1',
      title: 'My Challenge',
      description: 'desc',
      category: 'cat',
      technology: 'ts'
    };

    service.duplicateChallenge(challenge).subscribe();

    expect(challengeServiceMock.addChallenge).toHaveBeenCalled();
    const payload = challengeServiceMock.addChallenge.calls.mostRecent().args[0];
    expect(payload.title).toBe('My Challenge (Copy)');
  });
});
