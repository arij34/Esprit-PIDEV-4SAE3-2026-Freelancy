package tn.esprit.matching.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.matching.dto.InvitationDTO;
import tn.esprit.matching.entity.Invitation;
import tn.esprit.matching.entity.InvitationStatus;
import tn.esprit.matching.service.InvitationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InvitationController.class)
class InvitationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvitationService invitationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void send_shouldParseBodyAndCallService() throws Exception {
        Invitation inv = new Invitation();
        inv.setId(1L);
        inv.setProjectId(10L);
        inv.setFreelancerId(20L);
        inv.setClientId(30L);
        inv.setMatchScore(90.0);
        inv.setStatus(InvitationStatus.PENDING);

        given(invitationService.sendInvitation(10L, 20L, 30L, 90.0))
                .willReturn(inv);

        Map<String, Object> body = Map.of(
                "projectId", 10,
                "freelancerId", 20,
                "clientId", 30,
                "matchScore", 90
        );

        mockMvc.perform(post("/invitations/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.projectId", is(10)))
                .andExpect(jsonPath("$.freelancerId", is(20)))
                .andExpect(jsonPath("$.matchScore", is(90.0)));
    }

    @Test
    void getForFreelancer_shouldReturnListOfInvitationDTO() throws Exception {
        InvitationDTO dto = new InvitationDTO();
        dto.setId(1L);
        dto.setProjectId(10L);
        dto.setStatus("PENDING");

        given(invitationService.getInvitationsForFreelancer(20L))
                .willReturn(List.of(dto));

        mockMvc.perform(get("/invitations/freelancer/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].projectId", is(10)))
                .andExpect(jsonPath("$[0].status", is("PENDING")));
    }

    @Test
    void getForCurrentFreelancer_shouldUseServiceAndIgnoreTokenForNow() throws Exception {
        InvitationDTO dto = new InvitationDTO();
        dto.setId(1L);
        dto.setProjectId(10L);

        given(invitationService.getInvitationsForFreelancer(20L))
                .willReturn(List.of(dto));

        mockMvc.perform(get("/invitations/freelancer/me")
                        .param("freelancerId", "20")
                        .header("Authorization", "Bearer abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].projectId", is(10)));
    }

    @Test
    void accept_shouldCallServiceWithToken() throws Exception {
        Invitation inv = new Invitation();
        inv.setId(1L);
        inv.setStatus(InvitationStatus.ACCEPTED);

        given(invitationService.accept(1L, "Bearer abc"))
                .willReturn(inv);

        mockMvc.perform(put("/invitations/1/accept")
                        .header("Authorization", "Bearer abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACCEPTED")));
    }

    @Test
    void decline_shouldReturnUpdatedInvitation() throws Exception {
        Invitation inv = new Invitation();
        inv.setId(1L);
        inv.setStatus(InvitationStatus.DECLINED);

        given(invitationService.decline(1L)).willReturn(inv);

        mockMvc.perform(put("/invitations/1/decline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("DECLINED")));
    }

    @Test
    void pendingCount_shouldReturnCountInJson() throws Exception {
        given(invitationService.getPendingCount(20L))
                .willReturn(3L);

        mockMvc.perform(get("/invitations/freelancer/20/pending-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(3)));
    }

    @Test
    void trash_shouldReturnTrashedInvitation() throws Exception {
        Invitation inv = new Invitation();
        inv.setId(1L);
        inv.setStatus(InvitationStatus.TRASH);
        inv.setTrashedAt(LocalDateTime.now());

        given(invitationService.trash(1L)).willReturn(inv);

        mockMvc.perform(put("/invitations/1/trash"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("TRASH")));
    }

    @Test
    void restore_shouldReturnRestoredInvitation() throws Exception {
        Invitation inv = new Invitation();
        inv.setId(1L);
        inv.setStatus(InvitationStatus.PENDING);

        given(invitationService.restore(1L)).willReturn(inv);

        mockMvc.perform(put("/invitations/1/restore"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    void deletePermanently_shouldReturnOkMessage() throws Exception {
        mockMvc.perform(delete("/invitations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Deleted permanently")));

        verify(invitationService).deletePermanently(1L);
    }

    @Test
    void getTrash_shouldReturnTrashList() throws Exception {
        InvitationDTO dto = new InvitationDTO();
        dto.setId(1L);
        dto.setProjectId(10L);
        dto.setStatus("TRASH");

        given(invitationService.getTrash(20L))
                .willReturn(List.of(dto));

        mockMvc.perform(get("/invitations/freelancer/20/trash"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status", is("TRASH")));
    }
}