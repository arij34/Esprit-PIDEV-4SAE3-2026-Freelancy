package tn.esprit.matching.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.matching.clients.InvitationProjectDTO;
import tn.esprit.matching.clients.ProjectClient;
import tn.esprit.matching.dto.AdminInvitationDTO;
import tn.esprit.matching.dto.InvitationDTO;
import tn.esprit.matching.entity.ApplicationResponse;
import tn.esprit.matching.entity.Invitation;
import tn.esprit.matching.entity.InvitationStatus;
import tn.esprit.matching.entity.Matching;
import tn.esprit.matching.repository.ApplicationResponseRepository;
import tn.esprit.matching.repository.InvitationRepository;
import tn.esprit.matching.repository.MatchingRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    @Mock
    private UserContextService userContextService;

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private ProjectClient projectClient;

    @Mock
    private MatchingRepository matchingRepository;

    @Mock
    private ApplicationResponseRepository applicationResponseRepository;

    @InjectMocks
    private InvitationService invitationService;

    // ---------- sendInvitation ----------

    @Test
    void sendInvitation_whenInvitationAlreadyExists_shouldThrow() {
        Long projectId = 1L;
        Long freelancerId = 2L;
        Long clientId = 3L;

        given(invitationRepository.existsByProjectIdAndFreelancerId(projectId, freelancerId))
                .willReturn(true);

        assertThatThrownBy(() ->
                invitationService.sendInvitation(projectId, freelancerId, clientId, 80.0))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invitation already sent");

        verify(invitationRepository, never()).save(any());
    }

    @Test
    void sendInvitation_whenNoExistingInvitation_shouldSaveWithPendingStatus() {
        Long projectId = 1L;
        Long freelancerId = 2L;
        Long clientId = 3L;
        Double score = 90.0;

        given(invitationRepository.existsByProjectIdAndFreelancerId(projectId, freelancerId))
                .willReturn(false);

        ArgumentCaptor<Invitation> captor = ArgumentCaptor.forClass(Invitation.class);
        given(invitationRepository.save(any(Invitation.class)))
                .willAnswer(invocation -> {
                    Invitation inv = invocation.getArgument(0);
                    inv.setId(10L);
                    return inv;
                });

        Invitation saved = invitationService.sendInvitation(projectId, freelancerId, clientId, score);

        assertThat(saved.getId()).isEqualTo(10L);
        assertThat(saved.getProjectId()).isEqualTo(projectId);
        assertThat(saved.getFreelancerId()).isEqualTo(freelancerId);
        assertThat(saved.getClientId()).isEqualTo(clientId);
        assertThat(saved.getMatchScore()).isEqualTo(score);
        assertThat(saved.getStatus()).isEqualTo(InvitationStatus.PENDING);

        verify(invitationRepository).save(captor.capture());
        Invitation passed = captor.getValue();
        assertThat(passed.getStatus()).isEqualTo(InvitationStatus.PENDING);
    }

    // ---------- getInvitationsForFreelancer ----------

    @Test
    void getInvitationsForFreelancer_shouldBuildDTOsWithProjectData() {
        Long freelancerId = 5L;

        Invitation inv = new Invitation();
        inv.setId(1L);
        inv.setFreelancerId(freelancerId);
        inv.setProjectId(100L);
        inv.setStatus(InvitationStatus.PENDING);
        inv.setMatchScore(88.0);
        inv.setCreatedAt(LocalDateTime.of(2026, 4, 1, 10, 0));

        given(invitationRepository.findByFreelancerId(freelancerId))
                .willReturn(List.of(inv));

        InvitationProjectDTO projectDto = new InvitationProjectDTO();
        projectDto.setTitle("My Project");
        projectDto.setDescription("Desc");
        projectDto.setClientName("Client Name");
        projectDto.setClientEmail("client@example.com");
        projectDto.setDeadline(String.valueOf(LocalDateTime.of(2026, 5, 1, 10, 0)));
        projectDto.setBudgetMin(100);
        projectDto.setBudgetMax(1000);
        projectDto.setBudgetRecommended(500);
        projectDto.setDurationEstimatedWeeks(4);
        projectDto.setRequiredSkills(List.of("Java", "Spring"));

        given(projectClient.getInvitationData(100L)).willReturn(projectDto);

        List<InvitationDTO> dtos = invitationService.getInvitationsForFreelancer(freelancerId);

        assertThat(dtos).hasSize(1);
        InvitationDTO dto = dtos.get(0);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getProjectId()).isEqualTo(100L);
        assertThat(dto.getProjectTitle()).isEqualTo("My Project");
        assertThat(dto.getClientName()).isEqualTo("Client Name");
        assertThat(dto.getClientEmail()).isEqualTo("client@example.com");
        assertThat(dto.getStatus()).isEqualTo("PENDING");
        assertThat(dto.getMatchScore()).isEqualTo(88.0);
    }

    // ---------- accept ----------

    @Test
    void accept_whenInvitationNotFound_shouldThrow() {
        given(invitationRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> invitationService.accept(1L, "Bearer token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invitation not found");
    }

    @Test
    void accept_whenStatusNotPending_shouldThrow() {
        Invitation inv = new Invitation();
        inv.setId(1L);
        inv.setStatus(InvitationStatus.ACCEPTED);

        given(invitationRepository.findById(1L)).willReturn(Optional.of(inv));

        assertThatThrownBy(() -> invitationService.accept(1L, "Bearer token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    void accept_whenOk_shouldSetStatusAndCallProjectClient() {
        Invitation inv = new Invitation();
        inv.setId(1L);
        inv.setProjectId(100L);
        inv.setFreelancerId(200L);
        inv.setStatus(InvitationStatus.PENDING);

        given(invitationRepository.findById(1L)).willReturn(Optional.of(inv));

        // token -> sub
        given(userContextService.getCurrentKeycloakSub("Bearer abc"))
                .willReturn("kc-sub-123");

        // réponse au formulaire
        ApplicationResponse app = new ApplicationResponse();
        app.setAnswerQ1("Cover letter");
        app.setAnswerQ3("5 weeks");
        app.setAnswerQ4("1500");
        app.setAnswerQ5("Question to client");
        given(applicationResponseRepository.findFirstByInvitationId(1L))
                .willReturn(app);

        // getProposalByProjectAndFreelancer -> renvoie un map contenant un id
        Map<String, Object> proposalInfo = new HashMap<>();
        proposalInfo.put("id", 99L);
        given(projectClient.getProposalByProjectAndFreelancer(100L, 200L))
                .willReturn(proposalInfo);

        // on n’a PAS besoin de doNothing() sur les méthodes void,
        // Mockito ne fait rien par défaut.

        Invitation result = invitationService.accept(1L, "Bearer abc");

        assertThat(result.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        assertThat(result.getRespondedAt()).isNotNull();

        verify(projectClient).createProposalFromMatching(argThat(body ->
                body.get("projectId").equals(100L) &&
                        body.get("freelancerId").equals(200L) &&
                        body.get("freelancerKeycloakId").equals("kc-sub-123")
        ));

        verify(projectClient).updateProposalStatus(eq(99L), argThat(statusBody ->
                "ACCEPTED".equals(statusBody.get("status"))
        ));
    }
    // ---------- decline ----------

    @Test
    void decline_whenInvitationNotFound_shouldThrow() {
        given(invitationRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> invitationService.decline(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invitation not found");
    }

    @Test
    void decline_whenStatusNotPending_shouldThrow() {
        Invitation inv = new Invitation();
        inv.setId(1L);
        inv.setStatus(InvitationStatus.ACCEPTED);

        given(invitationRepository.findById(1L)).willReturn(Optional.of(inv));

        assertThatThrownBy(() -> invitationService.decline(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    void decline_whenOk_shouldSetDeclinedAndUpdateMatching() {
        Invitation inv = new Invitation();
        inv.setId(1L);
        inv.setProjectId(100L);
        inv.setFreelancerId(200L);
        inv.setStatus(InvitationStatus.PENDING);

        given(invitationRepository.findById(1L)).willReturn(Optional.of(inv));

        Matching matching = new Matching();
        matching.setProjectId(100L);
        matching.setFreelancerId(200L);
        matching.setStatus("CALCULATED");

        given(matchingRepository.findByFreelancerIdAndProjectId(200L, 100L))
                .willReturn(matching);

        Invitation result = invitationService.decline(1L);

        assertThat(result.getStatus()).isEqualTo(InvitationStatus.DECLINED);
        assertThat(result.getRespondedAt()).isNotNull();

        verify(matchingRepository).save(argThat(m ->
                "DECLINED".equals(m.getStatus())
        ));
    }

    // ---------- getPendingCount ----------

    @Test
    void getPendingCount_shouldDelegateToRepository() {
        Long freelancerId = 5L;
        given(invitationRepository.countByFreelancerIdAndStatus(freelancerId, InvitationStatus.PENDING))
                .willReturn(3L);

        long result = invitationService.getPendingCount(freelancerId);

        assertThat(result).isEqualTo(3L);
    }

    // ---------- trash ----------

    @Test
    void trash_whenStatusPendingOrDeclined_shouldSetTrashStatusAndTrashedAt() {
        Invitation inv = new Invitation();
        inv.setId(1L);
        inv.setStatus(InvitationStatus.PENDING);

        given(invitationRepository.findById(1L)).willReturn(Optional.of(inv));
        given(invitationRepository.save(any(Invitation.class))).willAnswer(invocation -> invocation.getArgument(0));

        Invitation trashed = invitationService.trash(1L);

        assertThat(trashed.getStatus()).isEqualTo(InvitationStatus.TRASH);
        assertThat(trashed.getTrashedAt()).isNotNull();
    }

    @Test
    void trash_whenStatusIsNotPendingOrDeclined_shouldThrow() {
        Invitation inv = new Invitation();
        inv.setId(1L);
        inv.setStatus(InvitationStatus.ACCEPTED);

        given(invitationRepository.findById(1L)).willReturn(Optional.of(inv));

        assertThatThrownBy(() -> invitationService.trash(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot trash invitation with status: ACCEPTED");
    }

    // ---------- restore ----------

    @Test
    void restore_whenStatusTrashOrDeclined_shouldSetPendingAndClearFields() {
        Invitation inv = new Invitation();
        inv.setId(1L);
        inv.setStatus(InvitationStatus.TRASH);
        inv.setTrashedAt(LocalDateTime.now());
        inv.setRespondedAt(LocalDateTime.now());

        given(invitationRepository.findById(1L)).willReturn(Optional.of(inv));
        given(invitationRepository.save(any(Invitation.class))).willAnswer(invocation -> invocation.getArgument(0));

        Invitation restored = invitationService.restore(1L);

        assertThat(restored.getStatus()).isEqualTo(InvitationStatus.PENDING);
        assertThat(restored.getTrashedAt()).isNull();
        assertThat(restored.getRespondedAt()).isNull();
    }

    @Test
    void restore_whenStatusIsNotTrashOrDeclined_shouldThrow() {
        Invitation inv = new Invitation();
        inv.setId(1L);
        inv.setStatus(InvitationStatus.ACCEPTED);

        given(invitationRepository.findById(1L)).willReturn(Optional.of(inv));

        assertThatThrownBy(() -> invitationService.restore(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot restore from status: ACCEPTED");
    }

    // ---------- deletePermanently ----------

    @Test
    void deletePermanently_shouldCallDeleteById() {
        invitationService.deletePermanently(10L);
        verify(invitationRepository).deleteById(10L);
    }

    // ---------- getTrash ----------

    @Test
    void getTrash_shouldReturnListOfInvitationDTOWithProjectData() {
        Long freelancerId = 7L;

        Invitation inv = new Invitation();
        inv.setId(1L);
        inv.setFreelancerId(freelancerId);
        inv.setProjectId(100L);
        inv.setStatus(InvitationStatus.TRASH);
        inv.setMatchScore(85.0);
        inv.setTrashedAt(LocalDateTime.now().minusDays(1));
        inv.setCreatedAt(LocalDateTime.now().minusDays(2));

        given(invitationRepository.findByFreelancerIdAndStatus(freelancerId, InvitationStatus.TRASH))
                .willReturn(List.of(inv));

        InvitationProjectDTO project = new InvitationProjectDTO();
        project.setTitle("Project title");
        project.setDescription("Desc");
        project.setClientName("Client");
        project.setBudgetMin(100);
        project.setBudgetMax(500);
        project.setBudgetRecommended(300);
        project.setDurationEstimatedWeeks(6);
        project.setRequiredSkills(Collections.emptyList());

        given(projectClient.getInvitationData(100L))
                .willReturn(project);

        List<InvitationDTO> result = invitationService.getTrash(freelancerId);

        assertThat(result).hasSize(1);
        InvitationDTO dto = result.get(0);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getProjectId()).isEqualTo(100L);
        assertThat(dto.getProjectTitle()).isEqualTo("Project title");
        assertThat(dto.getStatus()).isEqualTo("TRASH");
        assertThat(dto.getMatchScore()).isEqualTo(85.0);
    }

    // ---------- autoDeleteTrashed ----------

    @Test
    void autoDeleteTrashed_shouldDeleteInvitationsReturnedByRepository() {
        Invitation inv1 = new Invitation();
        inv1.setId(1L);
        inv1.setStatus(InvitationStatus.TRASH);
        inv1.setTrashedAt(LocalDateTime.now().minusMinutes(10));

        given(invitationRepository.findByStatusAndTrashedAtBefore(
                eq(InvitationStatus.TRASH),
                any(LocalDateTime.class)
        )).willReturn(List.of(inv1));

        invitationService.autoDeleteTrashed();

        verify(invitationRepository).deleteAll(List.of(inv1));
    }

    // ---------- getAllInvitationsForAdmin ----------

    @Test
    void getAllInvitationsForAdmin_shouldMapEntitiesToAdminInvitationDTO() {
        Invitation inv1 = new Invitation();
        inv1.setId(1L);
        inv1.setProjectId(100L);
        inv1.setFreelancerId(200L);
        inv1.setStatus(InvitationStatus.PENDING);
        inv1.setTrashedAt(null);

        Invitation inv2 = new Invitation();
        inv2.setId(2L);
        inv2.setProjectId(101L);
        inv2.setFreelancerId(201L);
        inv2.setStatus(InvitationStatus.TRASH);
        inv2.setTrashedAt(LocalDateTime.now());

        given(invitationRepository.findAllByOrderByCreatedAtDesc())
                .willReturn(List.of(inv1, inv2));

        List<AdminInvitationDTO> result = invitationService.getAllInvitationsForAdmin();

        assertThat(result).hasSize(2);
        AdminInvitationDTO dto1 = result.get(0);
        assertThat(dto1.getId()).isEqualTo(1L);
        assertThat(dto1.getProjectId()).isEqualTo(100L);
        assertThat(dto1.getFreelancerId()).isEqualTo(200L);
        assertThat(dto1.getStatus()).isEqualTo("PENDING");

        AdminInvitationDTO dto2 = result.get(1);
        assertThat(dto2.getStatus()).isEqualTo("TRASH");
        assertThat(dto2.getTrashedAt()).isNotNull();
    }
}