package tn.esprit.challengeservice.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.challengeservice.dtos.TaskDTO;
import tn.esprit.challengeservice.entities.Task;
import tn.esprit.challengeservice.entities.TaskStatus;
import tn.esprit.challengeservice.services.itaskService;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins:http://localhost:4200}")
public class TaskController {

    private final itaskService taskService;

    @PostMapping("/{challengeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Task addTask(@PathVariable String challengeId, @RequestBody TaskDTO taskDto) {
        return taskService.addTask(challengeId, toTaskEntity(taskDto));
    }

    @GetMapping("/challenge/{challengeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FREELANCER')")
    public List<Task> getTasksByChallenge(@PathVariable String challengeId) {
        return taskService.getTasksByChallenge(challengeId);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Task updateTask(@PathVariable String id, @RequestBody TaskDTO taskDto) {
        return taskService.updateTask(id, toTaskEntity(taskDto));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FREELANCER')")
    public Task updateTaskStatus(@PathVariable String id, @RequestParam String status) {
        try {
            TaskStatus taskStatus = TaskStatus.valueOf(status.toUpperCase().trim());
            return taskService.updateTaskStatus(id, taskStatus);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status. Valid values: COMPLETE, COMPLETED, INPROGRESS, INCOMPLETE, CLOSED, ACTIVE");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
    }

    private Task toTaskEntity(TaskDTO dto) {
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus());
        task.setSubmittedAt(dto.getSubmittedAt());
        task.setDeadline(dto.getDeadline());
        task.setProgress(dto.getProgress());
        return task;
    }
}