package tn.esprit.matching.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.matching.clients.InvitationProjectDTO;
import tn.esprit.matching.clients.ProjectClient;
import tn.esprit.matching.dto.FormResponseRequest;
import tn.esprit.matching.entity.ApplicationResponse;
import tn.esprit.matching.entity.Invitation;
import tn.esprit.matching.repository.ApplicationResponseRepository;
import tn.esprit.matching.repository.InvitationRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationResponseServiceTest {

    @Mock
    private ApplicationResponseRepository applicationResponseRepository;

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private ProjectClient projectClient;

    @InjectMocks
    private ApplicationResponseService applicationResponseService;

    private Invitation invitation;
    private FormResponseRequest request;
    private InvitationProjectDTO projectDto;

    @BeforeEach
    void setUp() {
        invitation = new Invitation();
        invitation.setId(1L);
        invitation.setProjectId(100L);
        invitation.setFreelancerId(200L);

        request = new FormResponseRequest();
        request.setInvitationId(1L);
        request.setQ1("Motivation");
        request.setQ2("Autre");
        request.setQ3("4 weeks");
        request.setQ4("2000");
        request.setQ5("Question au client");

        projectDto = new InvitationProjectDTO();
        projectDto.setBudgetMax(3000);
        projectDto.setDurationEstimatedWeeks(6);
    }

    @Test
    void saveResponse_whenInvitationNotFound_shouldThrow() {
        // given
        given(invitationRepository.findById(1L)).willReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> applicationResponseService.saveResponse(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invitation not found");

        verify(invitationRepository).findById(1L);
        verifyNoInteractions(applicationResponseRepository);
    }

    @Test
    void saveResponse_whenQ1TooLong_shouldThrowIllegalArgumentException() {
        // given
        given(invitationRepository.findById(1L)).willReturn(Optional.of(invitation));
        given(projectClient.getInvitationData(100L)).willReturn(projectDto);

        String longText = "x".repeat(2001);
        request.setQ1(longText);

        // when + then
        assertThatThrownBy(() -> applicationResponseService.saveResponse(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Answer Q1 must not exceed 2000 characters");
    }

    @Test
    void saveResponse_whenBudgetExceedsMax_shouldThrowIllegalArgumentException() {
        // given
        given(invitationRepository.findById(1L)).willReturn(Optional.of(invitation));
        projectDto.setBudgetMax(1000); // max plus petit
        given(projectClient.getInvitationData(100L)).willReturn(projectDto);

        request.setQ4("2000"); // budget proposé > budgetMax

        // when + then
        assertThatThrownBy(() -> applicationResponseService.saveResponse(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Your proposed budget (2000.0) cannot exceed");
    }

    @Test
    void saveResponse_whenTimelineTooLong_shouldThrowIllegalArgumentException() {
        // given
        given(invitationRepository.findById(1L)).willReturn(Optional.of(invitation));
        projectDto.setDurationEstimatedWeeks(4); // max 4 semaines
        given(projectClient.getInvitationData(100L)).willReturn(projectDto);

        request.setQ3("I can do it in 8 weeks"); // > 4

        // when + then
        assertThatThrownBy(() -> applicationResponseService.saveResponse(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Your delivery timeline (8 weeks) cannot exceed");
    }

    @Test
    void saveResponse_whenFirstSubmission_shouldCreateNewApplicationResponse() {
        // given
        given(invitationRepository.findById(1L)).willReturn(Optional.of(invitation));
        given(projectClient.getInvitationData(100L)).willReturn(projectDto);
        given(applicationResponseRepository.findByInvitationId(1L))
                .willReturn(Optional.empty());

        // on simule le save qui renvoie l'entité avec un id
        ArgumentCaptor<ApplicationResponse> captor = ArgumentCaptor.forClass(ApplicationResponse.class);
        given(applicationResponseRepository.save(any(ApplicationResponse.class)))
                .willAnswer(invocation -> {
                    ApplicationResponse ar = invocation.getArgument(0);
                    ar.setId(10L);
                    return ar;
                });

        // when
        ApplicationResponse saved = applicationResponseService.saveResponse(request);

        // then
        assertThat(saved.getId()).isEqualTo(10L);
        assertThat(saved.getInvitationId()).isEqualTo(1L);
        assertThat(saved.getFreelancerId()).isEqualTo(200L);
        assertThat(saved.getProjectId()).isEqualTo(100L);
        assertThat(saved.getAnswerQ1()).isEqualTo("Motivation");

        verify(applicationResponseRepository).save(captor.capture());
        ApplicationResponse passed = captor.getValue();
        assertThat(passed.getInvitationId()).isEqualTo(1L);
    }

    @Test
    void saveResponse_whenEditAfter24Hours_shouldThrowIllegalStateException() {
        // given
        given(invitationRepository.findById(1L)).willReturn(Optional.of(invitation));
        given(projectClient.getInvitationData(100L)).willReturn(projectDto);

        ApplicationResponse existing = new ApplicationResponse();
        existing.setId(5L);
        existing.setInvitationId(1L);
        existing.setCreatedAt(LocalDateTime.now().minusHours(25)); // +24h

        given(applicationResponseRepository.findByInvitationId(1L))
                .willReturn(Optional.of(existing));

        // when + then
        assertThatThrownBy(() -> applicationResponseService.saveResponse(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("You can no longer edit your answers after 24 hours.");
    }

    @Test
    void canEdit_whenNoResponse_shouldReturnTrue() {
        given(applicationResponseRepository.findByInvitationId(1L))
                .willReturn(Optional.empty());

        boolean result = applicationResponseService.canEdit(1L);

        assertThat(result).isTrue();
    }

    @Test
    void canEdit_whenCreatedLessThan24Hours_shouldReturnTrue() {
        ApplicationResponse ar = new ApplicationResponse();
        ar.setCreatedAt(LocalDateTime.now().minusHours(5));
        given(applicationResponseRepository.findByInvitationId(1L))
                .willReturn(Optional.of(ar));

        boolean result = applicationResponseService.canEdit(1L);

        assertThat(result).isTrue();
    }

    @Test
    void canEdit_whenCreatedMoreThan24Hours_shouldReturnFalse() {
        ApplicationResponse ar = new ApplicationResponse();
        ar.setCreatedAt(LocalDateTime.now().minusHours(30));
        given(applicationResponseRepository.findByInvitationId(1L))
                .willReturn(Optional.of(ar));

        boolean result = applicationResponseService.canEdit(1L);

        assertThat(result).isFalse();
    }
}