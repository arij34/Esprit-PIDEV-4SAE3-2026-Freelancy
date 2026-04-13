package tn.esprit.planningg.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import tn.esprit.planningg.dto.AiTaskSuggestionItem;
import tn.esprit.planningg.dto.AiTaskSuggestionRequest;
import tn.esprit.planningg.dto.AiTaskSuggestionResponse;
import tn.esprit.planningg.entities.Planning;
import tn.esprit.planningg.entities.Priorite;
import tn.esprit.planningg.entities.Statut;
import tn.esprit.planningg.entities.Task;
import tn.esprit.planningg.repositories.PlanningRepository;
import tn.esprit.planningg.repositories.TaskRepository;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class TaskServiceImpl implements ITaskService {
    private static final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final TaskRepository taskRepository;
    private final PlanningRepository planningRepository;
    private final JavaMailSender mailSender;
    private final String taskCreatedMailTo;
    private final String taskCreatedMailFrom;
        private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final String geminiApiKey;
    private final String geminiModel;
    private final String geminiBaseUrl;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public TaskServiceImpl(
            TaskRepository taskRepository,
            PlanningRepository planningRepository,
            JavaMailSender mailSender,
            @Value("${app.mail.task-created.to}") String taskCreatedMailTo,
            @Value("${app.mail.task-created.from}") String taskCreatedMailFrom,
            @Value("${gemini.api.key:}") String geminiApiKey,
            @Value("${gemini.model:gemini-2.5-flash}") String geminiModel,
            @Value("${gemini.base-url:https://generativelanguage.googleapis.com/v1beta/models}") String geminiBaseUrl
    ) {
        this.taskRepository = taskRepository;
        this.planningRepository = planningRepository;
        this.mailSender = mailSender;
        this.taskCreatedMailTo = taskCreatedMailTo;
        this.taskCreatedMailFrom = taskCreatedMailFrom;
        this.geminiApiKey = geminiApiKey;
        this.geminiModel = geminiModel;
        this.geminiBaseUrl = geminiBaseUrl;
    }

    @Override
    public Task addTask(Task task) {
        validateTaskDates(task);
        resolvePlanningIfPresent(task);
        assignAutomaticPriority(task);
        Task savedTask = taskRepository.save(task);
        sendTaskCreatedEmail(savedTask);
        return savedTask;
    }

    @Override
    public Task updateTask(Task task) {
        validateTaskDates(task);
        resolvePlanningIfPresent(task);
        assignAutomaticPriority(task);
        return taskRepository.save(task);
    }

    @Override
    public Task getTaskById(Long id) { return taskRepository.findById(id).orElse(null); }

    @Override
    public List<Task> getAllTasks() {
        return StreamSupport.stream(taskRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> getOverdueTasks() {
        LocalDateTime now = LocalDateTime.now();
        return taskRepository.findByDateFinBeforeAndStatutNot(now, Statut.DONE)
                .stream()
                .sorted(Comparator.comparing(Task::getDateFin, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

        @Override
        public AiTaskSuggestionResponse generateTaskSuggestions(AiTaskSuggestionRequest request) {
        if (request == null || request.getPlanningId() == null) {
            throw new IllegalArgumentException("planningId is required");
        }

        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            throw new IllegalStateException("Gemini API key is missing. Set gemini.api.key in application.properties");
        }

        Long planningId = request.getPlanningId();
        int targetCount = request.getTargetCount() == null ? 3 : Math.max(1, Math.min(request.getTargetCount(), 8));

        Planning planning = planningRepository.findById(planningId)
            .orElseThrow(() -> new IllegalArgumentException("Planning introuvable avec id=" + planningId));

        List<Task> existingTasks = taskRepository.findByPlanning_Id(planningId);
        String prompt = buildGeminiPrompt(planning, existingTasks, targetCount);

        try {
            String endpoint = geminiBaseUrl + "/" + geminiModel + ":generateContent?key="
                + URLEncoder.encode(geminiApiKey, StandardCharsets.UTF_8);

            String requestBody = objectMapper.writeValueAsString(
                java.util.Map.of(
                    "contents", List.of(
                        java.util.Map.of("parts", List.of(
                            java.util.Map.of("text", prompt)
                        ))
                    ),
                    "generationConfig", java.util.Map.of(
                        "temperature", 0.4,
                        "responseMimeType", "application/json"
                    )
                )
            );

            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
            throw new IllegalStateException("Gemini API error: HTTP " + response.statusCode());
            }

            List<AiTaskSuggestionItem> suggestions = parseGeminiSuggestions(response.body(), planningId, targetCount);

            AiTaskSuggestionResponse output = new AiTaskSuggestionResponse();
            output.setModel(geminiModel);
            output.setSuggestions(suggestions);
            return output;
        } catch (Exception ex) {
            log.error("Failed to generate AI suggestions for planning id={}", planningId, ex);
            throw new IllegalStateException("AI suggestions generation failed: " + ex.getMessage());
        }
        }

    @Override
    public void deleteTask(Long id) { taskRepository.deleteById(id); }

    private void resolvePlanningIfPresent(Task task) {
        if (task.getPlanning() == null || task.getPlanning().getId() == null) {
            task.setPlanning(null);
            return;
        }

        Long planningId = task.getPlanning().getId();
        Planning planning = planningRepository.findById(planningId)
                .orElseThrow(() -> new IllegalArgumentException("Planning introuvable avec id=" + planningId));
        task.setPlanning(planning);
    }

    private void validateTaskDates(Task task) {
        if (task.getDateDebut() == null || task.getDateFin() == null) {
            return;
        }

        if (task.getDateDebut().isAfter(task.getDateFin())) {
            throw new IllegalArgumentException("dateDebut doit etre inferieure ou egale a dateFin");
        }
    }

    private void assignAutomaticPriority(Task task) {
        LocalDateTime now = LocalDateTime.now();

        if (task.getStatut() == null) {
            task.setStatut(Statut.TODO);
        }

        if (task.getStatut() == Statut.DONE) {
            task.setPriorite(Priorite.LOW);
            return;
        }

        if (task.getDateFin() == null) {
            task.setPriorite(task.getStatut() == Statut.IN_PROGRESS ? Priorite.HIGH : Priorite.MEDIUM);
            return;
        }

        if (task.getDateFin().isBefore(now)) {
            task.setPriorite(Priorite.CRITICAL);
            return;
        }

        long hoursLeft = Duration.between(now, task.getDateFin()).toHours();
        Priorite computed;
        if (hoursLeft <= 24) {
            computed = Priorite.HIGH;
        } else if (hoursLeft <= 72) {
            computed = Priorite.MEDIUM;
        } else {
            computed = Priorite.LOW;
        }

        if (task.getStatut() == Statut.IN_PROGRESS && computed == Priorite.LOW) {
            computed = Priorite.MEDIUM;
        }

        task.setPriorite(computed);
    }

    private void sendTaskCreatedEmail(Task task) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(taskCreatedMailFrom);
            message.setTo(taskCreatedMailTo);
            message.setSubject("New task created: " + (task.getTitre() != null ? task.getTitre() : "(no title)"));
            message.setText(buildTaskCreatedEmailBody(task));
            mailSender.send(message);
        } catch (Exception ex) {
            // Keep task creation successful even if SMTP fails.
            log.warn("Task created but email notification failed for task id={}", task.getId(), ex);
        }
    }

    private String buildTaskCreatedEmailBody(Task task) {
        String planningType = "Without planning";

        if (task.getPlanning() != null) {
            if (task.getPlanning().getType() != null && !task.getPlanning().getType().isBlank()) {
                planningType = task.getPlanning().getType();
            }
        }

        StringBuilder body = new StringBuilder();
        body.append("A new task was created.").append("\n\n");
        //body.append("Task ID: ").append(task.getId()).append("\n");
        body.append("Title: ").append(task.getTitre()).append("\n");
        body.append("Description: ").append(task.getDescription()).append("\n");
        body.append("Start date: ").append(task.getDateDebut()).append("\n");
        body.append("End date: ").append(task.getDateFin()).append("\n");
        body.append("Status: ").append(task.getStatut()).append("\n");
        body.append("Priority: ").append(task.getPriorite()).append("\n");
        //body.append("Planning ID: ").append(planningId).append("\n");
        body.append("Planning type: ").append(planningType).append("\n");

        return body.toString();
    }

    private String buildGeminiPrompt(Planning planning, List<Task> existingTasks, int targetCount) {
        String existing = existingTasks.isEmpty()
                ? "No existing tasks yet."
                : existingTasks.stream()
                .map(t -> String.format(
                        "- titre=%s | description=%s | dateDebut=%s | dateFin=%s | statut=%s",
                        safe(t.getTitre()),
                        safe(t.getDescription()),
                    t.getDateDebut(),
                    t.getDateFin(),
                    t.getStatut()
                ))
                .collect(Collectors.joining("\n"));

        return "You are a project planning assistant. Analyze existing tasks and propose additional high-value, logical TODO tasks.\n"
                + "Language policy: auto-detect from existing tasks and keep same language.\n"
                + "Generate exactly " + targetCount + " tasks for planningId=" + planning.getId() + ".\n"
                + "Date policy: propose realistic near-future dates; ensure dateDebut <= dateFin; spread tasks logically without overlap conflicts when possible.\n"
                + "Statut must always be TODO for generated tasks.\n"
                + "Return STRICT JSON only (no markdown, no commentary) using this shape:\n"
                + "{\"suggestions\":[{\"titre\":\"...\",\"description\":\"...\",\"dateDebut\":\"2026-04-07T09:00:00\",\"dateFin\":\"2026-04-07T12:00:00\",\"statut\":\"TODO\",\"planningId\":" + planning.getId() + "}]}\n"
                + "Planning context:\n"
                + "- planningId=" + planning.getId() + "\n"
                + "- planningType=" + safe(planning.getType()) + "\n"
                + "Existing tasks:\n" + existing;
    }

    private List<AiTaskSuggestionItem> parseGeminiSuggestions(String rawApiResponse, Long planningId, int targetCount) throws Exception {
        JsonNode root = objectMapper.readTree(rawApiResponse);
        JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");

        if (textNode.isMissingNode() || textNode.asText().isBlank()) {
            throw new IllegalStateException("Gemini did not return text content");
        }

        String modelJson = textNode.asText().trim();
        JsonNode parsed = objectMapper.readTree(modelJson);

        List<AiTaskSuggestionItem> rawItems;
        if (parsed.isArray()) {
            rawItems = objectMapper.convertValue(parsed, new TypeReference<>() {});
        } else {
            JsonNode suggestionsNode = parsed.path("suggestions");
            if (!suggestionsNode.isArray()) {
                throw new IllegalStateException("Gemini response JSON has no suggestions array");
            }
            rawItems = objectMapper.convertValue(suggestionsNode, new TypeReference<>() {});
        }

        List<AiTaskSuggestionItem> sanitized = new ArrayList<>();
        LocalDateTime fallbackStart = LocalDate.now().plusDays(1).atTime(9, 0);

        for (AiTaskSuggestionItem item : rawItems) {
            if (item == null || item.getTitre() == null || item.getTitre().isBlank()) {
                continue;
            }

            if (item.getStatut() == null) {
                item.setStatut(Statut.TODO);
            }

            item.setStatut(Statut.TODO);
            item.setPlanningId(planningId);

            if (item.getDateDebut() == null) {
                item.setDateDebut(fallbackStart.plusDays(sanitized.size()));
            }
            if (item.getDateFin() == null || item.getDateFin().isBefore(item.getDateDebut())) {
                item.setDateFin(item.getDateDebut().plusHours(2));
            }
            if (item.getDescription() == null) {
                item.setDescription("");
            }

            sanitized.add(item);
            if (sanitized.size() == targetCount) {
                break;
            }
        }

        if (sanitized.isEmpty()) {
            throw new IllegalStateException("No valid suggestions returned by Gemini");
        }

        return sanitized;
    }

    private String safe(String value) {
        return value == null ? "" : value.replace("\n", " ").trim();
    }
}
