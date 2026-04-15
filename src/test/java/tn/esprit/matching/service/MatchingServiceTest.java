package tn.esprit.matching.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.matching.clients.*;
import tn.esprit.matching.dto.AdminMatchingRowDTO;
import tn.esprit.matching.dto.FreelancerMatchDTO;
import tn.esprit.matching.dto.FreelancerMatchedProjectDTO;
import tn.esprit.matching.entity.Matching;
import tn.esprit.matching.repository.InvitationRepository;
import tn.esprit.matching.repository.MatchingRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    @Mock
    private MatchingRepository matchingRepository;

    @Mock
    private CollectDataService collectDataService;

    @Mock
    private ScoreService scoreService;

    @Mock
    private SkillClient skillClient;

    @Mock
    private ProjectClient projectClient;

    @Mock
    private InvitationRepository invitationRepository;

    @InjectMocks
    private MatchingService matchingService;

    // ---------- getMatchedProjectIdsForFreelancer ----------

    @Test
    void getMatchedProjectIdsForFreelancer_whenNoMatchings_shouldReturnEmptyList() {
        Long freelancerId = 1L;
        given(matchingRepository.findByFreelancerId(freelancerId))
                .willReturn(List.of());

        List<FreelancerMatchedProjectDTO> result =
                matchingService.getMatchedProjectIdsForFreelancer(freelancerId);

        assertThat(result).isEmpty();
    }

    @Test
    void getMatchedProjectIdsForFreelancer_shouldReturnSortedByScoreDesc() {
        Long freelancerId = 1L;

        Matching m1 = new Matching();
        m1.setProjectId(100L);
        m1.setFreelancerId(freelancerId);
        m1.setScoreFinal(50.0);

        Matching m2 = new Matching();
        m2.setProjectId(200L);
        m2.setFreelancerId(freelancerId);
        m2.setScoreFinal(80.0);

        List<Matching> list = new java.util.ArrayList<>();
        list.add(m1);
        list.add(m2);
        given(matchingRepository.findByFreelancerId(freelancerId)).willReturn(list);

        List<FreelancerMatchedProjectDTO> result =
                matchingService.getMatchedProjectIdsForFreelancer(freelancerId);

        assertThat(result).hasSize(2);
        // tri décroissant → d'abord project 200, score 80
        assertThat(result.get(0).getProjectId()).isEqualTo(200L);
        assertThat(result.get(0).getMatchScore()).isEqualTo(80.0);
        assertThat(result.get(1).getProjectId()).isEqualTo(100L);
    }

    // ---------- getAllMatchingsForAdmin ----------

    @Test
    void getAllMatchingsForAdmin_shouldMapEntitiesToDto() {
        Matching m = new Matching();
        m.setId(1L);
        m.setProjectId(100L);
        m.setFreelancerId(200L);
        m.setScoreFinal(75.0);

        given(matchingRepository.findAllByOrderByProjectIdAscFreelancerIdAsc())
                .willReturn(List.of(m));

        List<AdminMatchingRowDTO> result = matchingService.getAllMatchingsForAdmin();

        assertThat(result).hasSize(1);
        AdminMatchingRowDTO dto = result.get(0);
        assertThat(dto.getProjectId()).isEqualTo(100L);
        assertThat(dto.getFreelancerId()).isEqualTo(200L);
        assertThat(dto.getScoreFinal()).isEqualTo(75.0);
    }

    // ---------- getMatchingForProject (cas simple avec 1 freelancer) ----------

    @Test
    void getMatchingForProject_withSingleFreelancer_shouldReturnDtoAndSaveMatching() {
        Long projectId = 10L;
        Long freelancerId = 20L;
        String token = "Bearer abc";

        // 1) skillClient.getAllUsers(token) -> liste avec notre freelancer
        UserDto user = new UserDto();
        user.setId(freelancerId);
        user.setFirstName("John");
        user.setLastName("Doe");

        given(skillClient.getAllUsers(token))
                .willReturn(List.of(user));

        // 2) collectDataService.getAllData(...) -> paquet de données fictives
        CollectDataService.MatchingDataPackage dataPackage =
                new CollectDataService.MatchingDataPackage(
                        new ExperienceMatchingResponse(),     // experience
                        new AvailabilityDTO(),                // availability
                        new EducationMatchingResponse(),      // education
                        List.of(new FreelancerSkillMatchingResponse()), // userSkills
                        new ProjectDTO(),                     // project
                        List.of(new ProjectSkillDTO()),       // projectSkills
                        new ProjectAnalysisDTO()              // analysis
                );

        given(collectDataService.getAllData(freelancerId, projectId))
                .willReturn(CompletableFuture.completedFuture(dataPackage));

        // 3) ScoreService retourne des scores arbitraires mais non nuls
        given(scoreService.scoreAvailability(any())).willReturn(80.0);
        given(scoreService.scoreEducation(any(), any())).willReturn(70.0);
        given(scoreService.scoreSkills(any(), any())).willReturn(90.0);
        given(scoreService.scoreExperience(any(), any())).willReturn(85.0);
        given(scoreService.calculateFinalScore(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .willReturn(88.0);

        // 4) matchingRepository.findByFreelancerIdAndProjectId -> pas encore de ligne
        given(matchingRepository.findByFreelancerIdAndProjectId(freelancerId, projectId))
                .willReturn(null);

        // 5) on capture l'objet Matching sauvegardé
        ArgumentCaptor<Matching> matchingCaptor = ArgumentCaptor.forClass(Matching.class);
        given(matchingRepository.save(any(Matching.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        List<FreelancerMatchDTO> result =
                matchingService.getMatchingForProject(projectId, List.of(freelancerId), token);

        // then
        assertThat(result).hasSize(1);
        FreelancerMatchDTO dto = result.get(0);
        assertThat(dto.getId()).isEqualTo(freelancerId);
        assertThat(dto.getFirstName()).isEqualTo("John");
        assertThat(dto.getLastName()).isEqualTo("Doe");
        assertThat(dto.getMatchScore()).isEqualTo(88.0); // arrondi dans le service

        verify(matchingRepository).save(matchingCaptor.capture());
        Matching saved = matchingCaptor.getValue();
        assertThat(saved.getProjectId()).isEqualTo(projectId);
        assertThat(saved.getFreelancerId()).isEqualTo(freelancerId);
        assertThat(saved.getScoreFinal()).isEqualTo(88.0);
        assertThat(saved.getStatus()).isEqualTo("CALCULATED");
    }

    // ---------- getMatchingForProjectAuto quand des matchings existent déjà ----------

    @Test
    void getMatchingForProjectAuto_whenExistingMatchings_shouldReuseFreelancerIds() {
        Long projectId = 10L;
        String token = "Bearer abc";

        Matching m1 = new Matching();
        m1.setProjectId(projectId);
        m1.setFreelancerId(1L);
        m1.setScoreFinal(70.0);

        Matching m2 = new Matching();
        m2.setProjectId(projectId);
        m2.setFreelancerId(2L);
        m2.setScoreFinal(80.0);

        given(matchingRepository.findByProjectId(projectId))
                .willReturn(List.of(m1, m2));

        // On stubbe getMatchingForProject pour ne pas réécrire toute la logique
        // mais vérifier qu'on l'appelle avec les bons IDs
        try (MockedStatic<LocalDate> ignored = null) {
            // stub simple : on laisse la vraie méthode tourner,
            // on stubbe juste ce dont elle a besoin :
            UserDto u1 = new UserDto();
            u1.setId(1L);
            u1.setFirstName("A");
            UserDto u2 = new UserDto();
            u2.setId(2L);
            u2.setFirstName("B");

            given(skillClient.getAllUsers(token)).willReturn(List.of(u1, u2));

            CollectDataService.MatchingDataPackage dataPackage =
                    new CollectDataService.MatchingDataPackage(
                            new ExperienceMatchingResponse(),
                            new AvailabilityDTO(),
                            new EducationMatchingResponse(),
                            List.of(new FreelancerSkillMatchingResponse()),
                            new ProjectDTO(),
                            List.of(new ProjectSkillDTO()),
                            new ProjectAnalysisDTO()
                    );

            given(collectDataService.getAllData(anyLong(), eq(projectId)))
                    .willReturn(CompletableFuture.completedFuture(dataPackage));

            given(scoreService.scoreAvailability(any())).willReturn(10.0);
            given(scoreService.scoreEducation(any(), any())).willReturn(10.0);
            given(scoreService.scoreSkills(any(), any())).willReturn(10.0);
            given(scoreService.scoreExperience(any(), any())).willReturn(10.0);
            given(scoreService.calculateFinalScore(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                    .willReturn(10.0);

            List<FreelancerMatchDTO> result =
                    matchingService.getMatchingForProjectAuto(projectId, token);

            assertThat(result).hasSize(2);
        }
    }
}