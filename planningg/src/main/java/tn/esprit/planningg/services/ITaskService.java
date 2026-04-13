package tn.esprit.planningg.services;
import tn.esprit.planningg.dto.AiTaskSuggestionRequest;
import tn.esprit.planningg.dto.AiTaskSuggestionResponse;
import tn.esprit.planningg.entities.Task;

import java.util.List;

public interface ITaskService {
    Task addTask(Task task);
    Task updateTask(Task task);
    Task getTaskById(Long id);
    List<Task> getAllTasks();
    List<Task> getOverdueTasks();
    AiTaskSuggestionResponse generateTaskSuggestions(AiTaskSuggestionRequest request);
    void deleteTask(Long id);
}