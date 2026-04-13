package tn.esprit.planningg.controllers;

import org.springframework.web.bind.annotation.*;
import tn.esprit.planningg.dto.AiTaskSuggestionRequest;
import tn.esprit.planningg.dto.AiTaskSuggestionResponse;
import tn.esprit.planningg.entities.Task;
import tn.esprit.planningg.services.ITaskService;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "http://localhost:4200")
public class TaskController {
    private final ITaskService taskService;

    public TaskController(ITaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public Task addTask(@RequestBody Task task) { return taskService.addTask(task); }

    @PostMapping("/ai-suggestions")
    public AiTaskSuggestionResponse generateAiSuggestions(@RequestBody AiTaskSuggestionRequest request) {
        return taskService.generateTaskSuggestions(request);
    }

    @GetMapping
    public List<Task> getAllTasks() { return taskService.getAllTasks(); }

    @GetMapping("/overdue")
    public List<Task> getOverdueTasks() { return taskService.getOverdueTasks(); }

    @GetMapping("/{id}")
    public Task getTask(@PathVariable Long id) { return taskService.getTaskById(id); }

    @PutMapping("/{id}")
    public Task updateTask(@PathVariable Long id, @RequestBody Task task) {
        task.setId(id);
        return taskService.updateTask(task);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id) { taskService.deleteTask(id); }
}
