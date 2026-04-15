package tn.esprit.contrat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tn.esprit.contrat.dto.AiContractResult;
import tn.esprit.contrat.dto.MilestoneRequest;
import tn.esprit.contrat.entity.ContractMilestone;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class ClaudeAiService {

    @Value("${anthropic.api.key}")
    private String apiKey;

    @Value("${anthropic.api.url}")
    private String apiUrl;

    @Value("${anthropic.api.model}")
    private String model;

    private final WebClient    webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ClaudeAiService(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    // =========================================================================
    // 1. Générer clauses + milestones (existant — inchangé)
    // =========================================================================

    public AiContractResult generateContractContent(
            String projectTitle,
            String projectDescription,
            BigDecimal totalAmount,
            String currency,
            LocalDate startDate,
            LocalDate endDate) {

        String prompt = buildPrompt(projectTitle, projectDescription, totalAmount, currency, startDate, endDate);

        try {
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model",      model);
            requestBody.put("max_tokens", 1500);
            requestBody.put("messages",   List.of(Map.of("role", "user", "content", prompt)));

            String rawResponse = webClient.post()
                    .uri(apiUrl)
                    .header("x-api-key",        apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseClaudeResponse(rawResponse, totalAmount, startDate, endDate);

        } catch (Exception e) {
            System.err.println("❌ Claude AI error: " + e.getMessage());
            return buildFallback(projectTitle, totalAmount, startDate, endDate);
        }
    }

    // =========================================================================
    // 2. ── NOUVEAU — Résumé du contrat en 5 points (pour le freelancer)
    // =========================================================================

    /**
     * Appelle Claude pour générer un résumé lisible du contrat en 5 points clés.
     *
     * @param title       titre du contrat
     * @param description description + clauses du contrat
     * @param milestones  liste des milestones
     * @param totalAmount montant total
     * @param currency    devise
     * @return liste de 5 points résumant le contrat
     */
    public List<String> summarizeContract(
            String title,
            String description,
            List<ContractMilestone> milestones,
            BigDecimal totalAmount,
            String currency) {

        String prompt = buildSummaryPrompt(title, description, milestones, totalAmount, currency);

        try {
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model",      model);
            requestBody.put("max_tokens", 800);
            requestBody.put("messages",   List.of(Map.of("role", "user", "content", prompt)));

            String rawResponse = webClient.post()
                    .uri(apiUrl)
                    .header("x-api-key",        apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseSummaryResponse(rawResponse);

        } catch (Exception e) {
            System.err.println("❌ Claude summary error: " + e.getMessage());
            return buildFallbackSummary(title, totalAmount, currency, milestones);
        }
    }

    // =========================================================================
    // 3. ── Résumé des MODIFICATIONS d'un contrat
    // =========================================================================

    /**
     * Génère un court résumé lisible des modifications proposées sur un contrat
     * (par exemple par le freelancer) à partir d'une description textuelle.
     * Le résultat est une ou deux phrases en français.
     */
    public String summarizeModifications(String modificationsDescription, String actorLabel) {
        if (modificationsDescription == null || modificationsDescription.isBlank()) {
            return "Modifications envoyées sur le contrat.";
        }

        String prompt = """
            Tu es un assistant juridique pour des contrats freelance.

            Rôle de la personne qui modifie : %s.

            Voici une description textuelle des modifications proposées sur un contrat :

            "%s"

            Ta tâche :
            - Générer un court résumé en français (1 à 2 phrases maximum)
            - Mettre en avant les points principaux modifiés (budget, délais, milestones, etc.)
            - Ne pas citer d'informations techniques inutiles.

            Réponds UNIQUEMENT avec le texte du résumé, sans JSON, sans markdown.
            """.formatted(actorLabel != null ? actorLabel : "Freelancer",
                    modificationsDescription.trim());

        try {
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model",      model);
            requestBody.put("max_tokens", 300);
            requestBody.put("messages",   List.of(Map.of("role", "user", "content", prompt)));

            String rawResponse = webClient.post()
                    .uri(apiUrl)
                    .header("x-api-key",        apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(rawResponse);
            String text   = root.path("content").get(0).path("text").asText();
            if (text != null && !text.isBlank()) {
                return text.trim();
            }

            // Si la réponse est vide ou mal formée, on génère un résumé localement.
            return buildLocalModificationsSummary(modificationsDescription, actorLabel);

        } catch (Exception e) {
            System.err.println("❌ Claude modifications summary error: " + e.getMessage());
            // Fallback lisible côté UI si l'appel IA échoue complètement
            return buildLocalModificationsSummary(modificationsDescription, actorLabel);
        }
    }

    /**
     * Fallback sans IA : génère un petit résumé lisible à partir de la
     * description HTML/textuelle des modifications.
     */
    private String buildLocalModificationsSummary(String modificationsDescription, String actorLabel) {
        if (modificationsDescription == null || modificationsDescription.isBlank()) {
            return "Modifications envoyées sur le contrat.";
        }

        String role = (actorLabel != null && !actorLabel.isBlank()) ? actorLabel.toLowerCase() : "freelancer";

        // Nettoyer le HTML de base (<br>, <strong>, &nbsp; ...)
        String cleaned = modificationsDescription
                .replace("<br>", " \n ")
                .replace("&nbsp;", " ")
                .replaceAll("<[^>]+>", "")
                .trim();

        if (cleaned.length() > 260) {
            cleaned = cleaned.substring(0, 260) + "...";
        }

        return "Les modifications proposées par le " + role
                + " concernent principalement : " + cleaned;
    }

    // =========================================================================
    // Prompts
    // =========================================================================

    private String buildPrompt(String projectTitle, String projectDescription,
                               BigDecimal totalAmount, String currency,
                               LocalDate startDate, LocalDate endDate) {
        return """
            You are a legal expert in freelance contracts.

            Generate a professional freelance contract for the following project:

            - Project Title: %s
            - Description: %s
            - Total Amount: %s %s
            - Start Date: %s
            - End Date: %s

            Respond ONLY with valid JSON, no markdown, no explanation.
            Exact expected format:
            {
              "description": "Professional contract description in 2-3 sentences",
              "clauses": [
                "Clause 1 - Deadlines: ...",
                "Clause 2 - Payments: ...",
                "Clause 3 - Intellectual Property: ...",
                "Clause 4 - Confidentiality: ...",
                "Clause 5 - Termination: ..."
              ],
              "milestones": [
                {
                  "title": "Phase 1 — Étude & Analyse",
                  "description": "Analyse des besoins, spécifications fonctionnelles et techniques, planification",
                  "amount": 0.00,
                  "deadlineDaysFromStart": 15,
                  "orderIndex": 1
                },
                {
                  "title": "Phase 2 — Design & Interface",
                  "description": "Conception UI/UX, maquettes, prototypes et validation client",
                  "amount": 0.00,
                  "deadlineDaysFromStart": 35,
                  "orderIndex": 2
                },
                {
                  "title": "Phase 3 — Développement & Codage",
                  "description": "Implémentation des fonctionnalités principales, développement back-end et front-end",
                  "amount": 0.00,
                  "deadlineDaysFromStart": 65,
                  "orderIndex": 3
                },
                {
                  "title": "Phase 4 — Tests & Validation",
                  "description": "Tests unitaires, tests d'intégration, correction des bugs et validation qualité",
                  "amount": 0.00,
                  "deadlineDaysFromStart": 85,
                  "orderIndex": 4
                },
                {
                  "title": "Phase 5 — Livraison & Déploiement",
                  "description": "Déploiement en production, documentation, formation et transfert de compétences",
                  "amount": 0.00,
                  "deadlineDaysFromStart": 100,
                  "orderIndex": 5
                }
              ]
            }

            IMPORTANT RULES:
            1. Milestone amounts must total EXACTLY %s %s (no more, no less)
            2. MANDATORY percentage split for the 5 phases:
               - Phase 1 Étude & Analyse      : 10%% of total
               - Phase 2 Design & Interface   : 20%% of total
               - Phase 3 Développement        : 40%% of total
               - Phase 4 Tests & Validation   : 20%% of total
               - Phase 5 Livraison            : 10%% of total
            3. Clauses must cover: deadlines, penalties, intellectual property, confidentiality, termination
            4. Respond ONLY with the JSON, nothing else
            5. ALL deadlineDaysFromStart values MUST be strictly between 1 and the total contract duration in days
            6. deadlineDaysFromStart for each phase: roughly 15%%, 35%%, 65%%, 85%%, 100%% of total duration
            7. The last milestone deadline must be <= total contract duration in days
            8. Always generate EXACTLY 5 milestones, no more, no less
            """.formatted(
                projectTitle,
                projectDescription != null ? projectDescription : "Development project",
                totalAmount, currency,
                startDate, endDate,
                totalAmount, currency
        );
    }

    private String buildSummaryPrompt(String title, String description,
                                      List<ContractMilestone> milestones,
                                      BigDecimal totalAmount, String currency) {

        // Construire un résumé textuel des milestones
        StringBuilder msText = new StringBuilder();
        if (milestones != null) {
            for (ContractMilestone m : milestones) {
                msText.append("- ").append(m.getTitle())
                        .append(" : ").append(m.getAmount()).append(" ").append(currency)
                        .append(" (deadline: ").append(m.getDeadline()).append(")\n");
            }
        }

        return """
            Tu es un assistant juridique spécialisé dans les contrats freelance.

            Voici les informations d'un contrat à signer :

            Titre : %s
            Montant total : %s %s
            Description et clauses :
            %s

            Milestones :
            %s

            Génère un résumé clair et concis de ce contrat en EXACTEMENT 5 points essentiels
            pour aider le freelancer à comprendre ce qu'il s'apprête à signer.

            Réponds UNIQUEMENT avec du JSON valide, sans markdown, sans explication.
            Format exact attendu :
            {
              "summary": [
                "Point 1 : ...",
                "Point 2 : ...",
                "Point 3 : ...",
                "Point 4 : ...",
                "Point 5 : ..."
              ]
            }

            Les points doivent couvrir : montant et paiements, délais et milestones,
            propriété intellectuelle, confidentialité, conditions de résiliation.
            Sois précis, simple et direct. Réponds UNIQUEMENT avec le JSON.
            """.formatted(
                title,
                totalAmount, currency,
                description != null ? description : "(non renseignée)",
                msText.toString()
        );
    }

    // =========================================================================
    // Parsers
    // =========================================================================

    private AiContractResult parseClaudeResponse(String rawResponse,
                                                 BigDecimal totalAmount,
                                                 LocalDate startDate,
                                                 LocalDate endDate) {
        try {
            JsonNode root    = objectMapper.readTree(rawResponse);
            String jsonText  = root.path("content").get(0).path("text").asText();
            jsonText = jsonText.replaceAll("```json", "").replaceAll("```", "").trim();

            JsonNode json = objectMapper.readTree(jsonText);

            String description = json.path("description").asText();

            List<String> clauses = new ArrayList<>();
            json.path("clauses").forEach(c -> clauses.add(c.asText()));

            List<MilestoneRequest> milestones = new ArrayList<>();
            json.path("milestones").forEach(m -> {
                MilestoneRequest ms = new MilestoneRequest();
                ms.setTitle(m.path("title").asText());
                ms.setDescription(m.path("description").asText());
                ms.setAmount(BigDecimal.valueOf(m.path("amount").asDouble()));
                ms.setOrderIndex(m.path("orderIndex").asInt());
                int daysFromStart = m.path("deadlineDaysFromStart").asInt(14);
                ms.setDeadline(startDate.plusDays(daysFromStart).toString());
                milestones.add(ms);
            });

            adjustMilestonesTotalAmount(milestones, totalAmount);
            return new AiContractResult(description, clauses, milestones);

        } catch (Exception e) {
            System.err.println("❌ Error parsing Claude response: " + e.getMessage());
            return buildFallback("Project", totalAmount, startDate, endDate);
        }
    }

    private List<String> parseSummaryResponse(String rawResponse) {
        try {
            JsonNode root    = objectMapper.readTree(rawResponse);
            String jsonText  = root.path("content").get(0).path("text").asText();
            jsonText = jsonText.replaceAll("```json", "").replaceAll("```", "").trim();

            JsonNode json = objectMapper.readTree(jsonText);

            List<String> summary = new ArrayList<>();
            json.path("summary").forEach(s -> summary.add(s.asText()));

            if (summary.isEmpty()) {
                throw new RuntimeException("Résumé vide retourné par Claude");
            }
            return summary;

        } catch (Exception e) {
            System.err.println("❌ Error parsing summary response: " + e.getMessage());
            return List.of("Erreur lors de la génération du résumé AI. Veuillez relire le contrat complet.");
        }
    }

    // =========================================================================
    // Fallbacks
    // =========================================================================

    private List<String> buildFallbackSummary(String title, BigDecimal totalAmount,
                                              String currency, List<ContractMilestone> milestones) {
        int nbMilestones = milestones != null ? milestones.size() : 0;
        return List.of(
                "Point 1 — Rémunération : Montant total du contrat " + totalAmount + " " + currency
                        + ", versé en " + nbMilestones + " milestone(s) selon les livrables validés par le client.",
                "Point 2 — Délais : Chaque milestone possède une date limite. Tout retard supérieur à 5 jours "
                        + "ouvrés entraîne une pénalité de 2% par semaine.",
                "Point 3 — Propriété intellectuelle : Tous les livrables produits dans le cadre de ce contrat "
                        + "deviennent la propriété exclusive du client après paiement intégral.",
                "Point 4 — Confidentialité : Le freelancer s'engage à ne divulguer aucune information "
                        + "confidentielle relative au projet pendant et après la durée du contrat.",
                "Point 5 — Résiliation : En cas de résiliation anticipée, le client règle les milestones "
                        + "complétés et approuvés. Le freelancer restitue tous les livrables partiels."
        );
    }

    private AiContractResult buildFallback(String projectTitle, BigDecimal totalAmount,
                                           LocalDate startDate, LocalDate endDate) {
        String description = "This freelance service contract is established for the project: "
                + projectTitle + ". It defines the terms and conditions between the client "
                + "and the freelancer for the completion of the agreed work.";

        List<String> clauses = new ArrayList<>();
        clauses.add("Clause 1 - Deadlines: The freelancer agrees to meet each milestone deadline. "
                + "Any delay exceeding 5 business days will incur a penalty of 2% per week.");
        clauses.add("Clause 2 - Payments: Payment for each milestone is triggered after client validation "
                + "within 5 business days following delivery.");
        clauses.add("Clause 3 - Intellectual Property: All deliverables become the exclusive property "
                + "of the client upon full payment of the contract.");
        clauses.add("Clause 4 - Confidentiality: The freelancer agrees not to disclose any confidential "
                + "project information during and after the contract period.");
        clauses.add("Clause 5 - Termination: In case of early termination, the client pays for completed "
                + "and approved milestones. The freelancer returns all partial deliverables.");

        long totalDays = startDate.until(endDate).getDays();
        List<MilestoneRequest> milestones = new ArrayList<>();

        // 5 phases dynamiques : Étude / Design / Codage / Tests / Livraison
        String[][] defaults = {
                {"Phase 1 — Étude & Analyse",
                        "Analyse des besoins, spécifications fonctionnelles et techniques, planification du projet"},
                {"Phase 2 — Design & Interface",
                        "Conception UI/UX, maquettes wireframes, prototypes interactifs et validation client"},
                {"Phase 3 — Développement & Codage",
                        "Implémentation des fonctionnalités principales, développement back-end et front-end"},
                {"Phase 4 — Tests & Validation",
                        "Tests unitaires, tests d'intégration, correction des bugs et validation qualité"},
                {"Phase 5 — Livraison & Déploiement",
                        "Déploiement en production, documentation technique, formation et transfert de compétences"}
        };
        // Pourcentages : 10% étude / 20% design / 40% codage / 20% tests / 10% livraison
        double[] ratios    = {0.10, 0.20, 0.40, 0.20, 0.10};
        // Position dans la durée : 15% / 35% / 65% / 85% / 100%
        double[] dayRatios = {0.15, 0.35, 0.65, 0.85, 1.00};

        BigDecimal remaining = totalAmount;
        for (int i = 0; i < 5; i++) {
            MilestoneRequest m = new MilestoneRequest();
            m.setTitle(defaults[i][0]);
            m.setDescription(defaults[i][1]);
            m.setOrderIndex(i + 1);
            long daysOffset = Math.max(1, (long)(totalDays * dayRatios[i]));
            m.setDeadline(startDate.plusDays(daysOffset).toString());
            // Dernière phase = reste exact pour éviter les erreurs d'arrondi
            BigDecimal amount = (i < 4)
                    ? totalAmount.multiply(BigDecimal.valueOf(ratios[i])).setScale(2, java.math.RoundingMode.HALF_UP)
                    : remaining;
            m.setAmount(amount);
            if (i < 4) remaining = remaining.subtract(amount);
            milestones.add(m);
        }

        return new AiContractResult(description, clauses, milestones);
    }

    // =========================================================================
    // PUBLIC — Fallback milestones (appelé depuis ContractServiceImpl si Claude AI échoue)
    // =========================================================================

    /**
     * Génère les 5 milestones standards en mode fallback (sans Claude AI).
     * Répartition : 10% Étude / 20% Design / 40% Codage / 20% Tests / 10% Livraison
     */
    public List<MilestoneRequest> buildFallbackMilestones(
            BigDecimal totalAmount,
            LocalDate startDate,
            LocalDate endDate) {

        if (startDate == null) startDate = LocalDate.now();
        if (endDate   == null) endDate   = startDate.plusMonths(3);
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) == 0)
            totalAmount = BigDecimal.ONE;

        long totalDays = startDate.until(endDate).getDays();
        if (totalDays < 5) totalDays = 30; // sécurité minimale

        String[][] phases = {
                {"Phase 1 — Étude & Analyse",
                        "Analyse des besoins, spécifications fonctionnelles et techniques, planification du projet"},
                {"Phase 2 — Design & Interface",
                        "Conception UI/UX, maquettes wireframes, prototypes interactifs et validation client"},
                {"Phase 3 — Développement & Codage",
                        "Implémentation des fonctionnalités principales, développement back-end et front-end"},
                {"Phase 4 — Tests & Validation",
                        "Tests unitaires, tests d'intégration, correction des bugs et validation qualité"},
                {"Phase 5 — Livraison & Déploiement",
                        "Déploiement en production, documentation technique, formation et transfert de compétences"}
        };
        double[] ratios    = {0.10, 0.20, 0.40, 0.20, 0.10};
        double[] dayRatios = {0.15, 0.35, 0.65, 0.85, 1.00};

        List<MilestoneRequest> milestones = new ArrayList<>();
        BigDecimal remaining = totalAmount;

        for (int i = 0; i < 5; i++) {
            MilestoneRequest m = new MilestoneRequest();
            m.setTitle(phases[i][0]);
            m.setDescription(phases[i][1]);
            m.setOrderIndex(i + 1);

            long daysOffset = Math.max(1, (long)(totalDays * dayRatios[i]));
            m.setDeadline(startDate.plusDays(daysOffset).toString());

            BigDecimal amount = (i < 4)
                    ? totalAmount.multiply(BigDecimal.valueOf(ratios[i]))
                    .setScale(2, java.math.RoundingMode.HALF_UP)
                    : remaining;
            m.setAmount(amount);
            if (i < 4) remaining = remaining.subtract(amount);
            milestones.add(m);
        }
        return milestones;
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private void adjustMilestonesTotalAmount(List<MilestoneRequest> milestones, BigDecimal totalAmount) {
        if (milestones.isEmpty()) return;
        BigDecimal currentTotal = milestones.stream()
                .map(MilestoneRequest::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (currentTotal.compareTo(BigDecimal.ZERO) == 0) {
            distributeEqually(milestones, totalAmount);
            return;
        }
        BigDecimal diff = totalAmount.subtract(currentTotal);
        if (diff.compareTo(BigDecimal.ZERO) != 0) {
            MilestoneRequest last = milestones.get(milestones.size() - 1);
            last.setAmount(last.getAmount().add(diff));
        }
    }

    private void distributeEqually(List<MilestoneRequest> milestones, BigDecimal totalAmount) {
        // Pourcentages par phase : 10% / 20% / 40% / 20% / 10%
        double[] ratios = {0.10, 0.20, 0.40, 0.20, 0.10};
        BigDecimal remaining = totalAmount;
        for (int i = 0; i < milestones.size(); i++) {
            if (i == milestones.size() - 1) {
                // Dernière phase = reste exact pour éviter les erreurs d'arrondi
                milestones.get(i).setAmount(remaining);
            } else {
                double ratio = (i < ratios.length) ? ratios[i] : (1.0 / milestones.size());
                BigDecimal amount = totalAmount.multiply(BigDecimal.valueOf(ratio))
                        .setScale(2, java.math.RoundingMode.HALF_UP);
                milestones.get(i).setAmount(amount);
                remaining = remaining.subtract(amount);
            }
        }
    }
}