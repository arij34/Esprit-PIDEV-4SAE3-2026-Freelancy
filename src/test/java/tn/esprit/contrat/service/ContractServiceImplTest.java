package tn.esprit.contrat.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.contrat.clients.UserServiceClient;
import tn.esprit.contrat.dto.AiContractResult;
import tn.esprit.contrat.dto.ContractCreateRequest;
import tn.esprit.contrat.dto.MilestoneRequest;
import tn.esprit.contrat.entity.*;
import tn.esprit.contrat.repository.ContractHistoryRepository;
import tn.esprit.contrat.repository.ContractMilestoneRepository;
import tn.esprit.contrat.repository.ContractRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractServiceImplTest {

    @Mock
    private ContractRepository contractRepo;
    @Mock
    private ContractMilestoneRepository milestoneRepo;
    @Mock
    private ContractHistoryRepository historyRepo;
    @Mock
    private ClaudeAiService claudeAiService;
    @Mock
    private PdfGeneratorService pdfGeneratorService;
    @Mock
    private UserServiceClient userServiceClient;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private ContractServiceImpl contractService;

    @Test
    void getContractById_returnsContract_whenExists() {
        Long id = 1L;
        Contract c = new Contract();
        c.setId(id);
        when(contractRepo.findById(id)).thenReturn(Optional.of(c));

        Contract result = contractService.getContractById(id);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        verify(contractRepo).findById(id);
    }

    @Test
    void submitForSignature_throws_ifNotDraft() {
        Long id = 2L;
        Contract c = new Contract();
        c.setId(id);
        c.setStatus(ContractStatus.ACTIVE);
        when(contractRepo.findById(id)).thenReturn(Optional.of(c));

        assertThatThrownBy(() -> contractService.submitForSignature(id, "token"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Seuls les contrats en DRAFT");
    }

    @Test
    void submitForSignature_throws_ifNoMilestones() {
        Long id = 3L;
        Contract c = new Contract();
        c.setId(id);
        c.setStatus(ContractStatus.DRAFT);
        c.setTotalAmount(BigDecimal.TEN);
        c.setMilestones(List.of());
        when(contractRepo.findById(id)).thenReturn(Optional.of(c));

        assertThatThrownBy(() -> contractService.submitForSignature(id, "token"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("au moins un milestone");
    }

    @Test
    void submitForSignature_setsPendingSignature_whenValid() {
        Long id = 4L;
        Contract c = new Contract();
        c.setId(id);
        c.setStatus(ContractStatus.DRAFT);
        c.setTotalAmount(BigDecimal.TEN);

        ContractMilestone m = new ContractMilestone();
        m.setAmount(BigDecimal.TEN);
        c.setMilestones(List.of(m));

        when(contractRepo.findById(id)).thenReturn(Optional.of(c));
        when(contractRepo.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Contract saved = contractService.submitForSignature(id, "token");

        assertThat(saved.getStatus()).isEqualTo(ContractStatus.PENDING_SIGNATURE);
        verify(historyRepo).save(any(ContractHistory.class));
        verify(emailService).sendSignatureRequestToFreelancer(any(Contract.class), eq("token"));
    }

    @Test
    void createContractInternal_generatesContractWithMilestonesAndHistory() {
        ContractCreateRequest request = new ContractCreateRequest();
        request.setTitle("Test contract");
        request.setDescription("Desc");
        request.setTotalAmount(BigDecimal.valueOf(100));
        request.setCurrency("TND");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(10));

        MilestoneRequest milestoneRequest = new MilestoneRequest();
        milestoneRequest.setTitle("M1");
        milestoneRequest.setDescription("d");
        milestoneRequest.setAmount(BigDecimal.TEN);
        milestoneRequest.setDeadline(null);
        milestoneRequest.setOrderIndex(null);

        AiContractResult aiResult = new AiContractResult(
            "AI desc",
            List.of("clause 1"),
            List.of(milestoneRequest)
        );
        when(claudeAiService.generateContractContent(any(), any(), any(), any(), any(), any()))
                .thenReturn(aiResult);

        Contract persisted = new Contract();
        persisted.setId(10L);
        persisted.setMilestones(new java.util.ArrayList<>());

        when(contractRepo.save(any(Contract.class))).thenReturn(persisted);

        Contract created = contractService.createContractInternal(request);

        assertThat(created.getStatus()).isEqualTo(ContractStatus.DRAFT);
        verify(contractRepo, atLeastOnce()).save(any(Contract.class));
        verify(historyRepo).save(any(ContractHistory.class));
    }

    @Test
    void getAllContracts_mapsToDtoWithUserNames() {
        Contract c = new Contract();
        c.setId(1L);
        c.setTitle("T");
        c.setClientId(2L);
        c.setMilestones(List.of());
        c.setClauses(List.of());
        when(contractRepo.findAllWithClauses()).thenReturn(List.of(c));
        when(userServiceClient.getAllUsers("token"))
                .thenReturn(List.of(Map.of("id", 2L, "firstName", "John", "lastName", "Doe")));

        var result = contractService.getAllContracts("token");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getClientName()).isEqualTo("John Doe");
    }
}
