package tn.esprit.challengeservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.challengeservice.entities.Task;
import tn.esprit.challengeservice.entities.TaskStatus;
import tn.esprit.challengeservice.services.itaskService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private itaskService taskService;

    @Test
    void addTask_shouldReturnSavedTask() throws Exception {
        Task task = Task.builder().idTask("t-1").title("Task 1").build();
        when(taskService.addTask(any(String.class), any(Task.class))).thenReturn(task);

        mockMvc.perform(post("/tasks/ch-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTask").value("t-1"));
    }

    @Test
    void getTasksByChallenge_shouldReturnList() throws Exception {
        when(taskService.getTasksByChallenge("ch-1")).thenReturn(List.of(Task.builder().idTask("t-1").build()));

        mockMvc.perform(get("/tasks/challenge/ch-1"))
                .andExpect(status().isOk());
    }

    @Test
    void updateTask_shouldReturnUpdatedTask() throws Exception {
        Task task = Task.builder().idTask("t-1").title("Updated").build();
        when(taskService.updateTask(any(String.class), any(Task.class))).thenReturn(task);

        mockMvc.perform(put("/tasks/t-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    @Test
    void updateTaskStatus_shouldReturnUpdatedTask() throws Exception {
        Task task = Task.builder().idTask("t-1").status(TaskStatus.COMPLETED).build();
        when(taskService.updateTaskStatus("t-1", TaskStatus.COMPLETED)).thenReturn(task);

        mockMvc.perform(patch("/tasks/t-1/status").param("status", "completed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void updateTaskStatus_whenInvalidStatus_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/tasks/t-1/status").param("status", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid status. Valid values: COMPLETE, COMPLETED, INPROGRESS, INCOMPLETE, CLOSED, ACTIVE"));
    }

    @Test
    void deleteTask_shouldReturnOk() throws Exception {
        doNothing().when(taskService).deleteTask("t-1");

        mockMvc.perform(delete("/tasks/t-1"))
                .andExpect(status().isOk());
    }
}
