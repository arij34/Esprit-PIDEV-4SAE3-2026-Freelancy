package tn.esprit.contrat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.contrat.dto.ContractCreateRequest;
import tn.esprit.contrat.dto.ContractResponseDTO;
import tn.esprit.contrat.dto.MilestoneRequest;
import tn.esprit.contrat.entity.Contract;
import tn.esprit.contrat.entity.ContractMilestone;
import tn.esprit.contrat.entity.ContractStatus;
import tn.esprit.contrat.service.ContractSigningScenarioService;
import tn.esprit.contrat.service.IContractService;
import tn.esprit.contrat.service.IDocuSignService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = ContractController.class)
class ContractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IContractService contractService;

    @MockBean
    private IDocuSignService docuSignService;

    @MockBean
    private ContractSigningScenarioService scenarioService;

    @Test
    void getAllContracts_returnsList() throws Exception {
        ContractResponseDTO dto = new ContractResponseDTO();
        dto.setId(1L);
        dto.setTitle("Test");
        when(contractService.getAllContracts(null)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/contracts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Test"));
    }

    @Test
    void createContract_returns201WithDto() throws Exception {
        ContractCreateRequest request = new ContractCreateRequest();
        request.setTitle("New");
        request.setDescription("Desc");
        request.setTotalAmount(BigDecimal.TEN);
        request.setCurrency("TND");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(1));

        Contract created = new Contract();
        created.setId(5L);
        created.setStatus(ContractStatus.DRAFT);

        when(contractService.createContract(any(ContractCreateRequest.class), eq("token")))
                .thenReturn(created);

        ContractResponseDTO dto = new ContractResponseDTO();
        dto.setId(5L);
        dto.setTitle("New");
        when(contractService.getAllContracts("token")).thenReturn(List.of(dto));

        mockMvc.perform(post("/api/contracts")
                        .header("Authorization", "token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.title").value("New"));
    }

    @Test
    void addMilestone_returnsCreated() throws Exception {
        MilestoneRequest req = new MilestoneRequest();
        req.setTitle("M1");
        req.setAmount(BigDecimal.TEN);

        ContractMilestone m = new ContractMilestone();
        m.setId(11L);
        m.setTitle("M1");
        when(contractService.addMilestone(eq(1L), any(MilestoneRequest.class), eq("token")))
                .thenReturn(m);

        mockMvc.perform(post("/api/contracts/1/milestones")
                        .header("Authorization", "token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(11L))
                .andExpect(jsonPath("$.title").value("M1"));
    }

    @Test
    void submitForSignature_badRequestOnBusinessError() throws Exception {
        when(contractService.submitForSignature(eq(1L), eq("token")))
                .thenThrow(new IllegalStateException("Erreur métier"));

        mockMvc.perform(put("/api/contracts/1/submit")
                        .header("Authorization", "token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Erreur métier"));
    }

    @Test
    void signContract_returnsOkWhenServiceOk() throws Exception {
        Contract c = new Contract();
        c.setId(1L);
        c.setStatus(ContractStatus.PENDING_SIGNATURE);
        when(contractService.getContractById(1L)).thenReturn(c);

        Contract saved = new Contract();
        saved.setId(1L);
        saved.setStatus(ContractStatus.PENDING_SIGNATURE);
        when(contractService.signContract(eq(1L), eq("FREELANCER"), eq("token"), any()))
                .thenReturn(saved);

        mockMvc.perform(put("/api/contracts/1/sign")
                        .param("role", "FREELANCER")
                        .header("Authorization", "token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contractId").value(1L));
    }
}
