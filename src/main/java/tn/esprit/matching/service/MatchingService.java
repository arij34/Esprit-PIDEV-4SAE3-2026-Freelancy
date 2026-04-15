package tn.esprit.matching.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.matching.clients.ProjectClient;
import tn.esprit.matching.clients.ProposalDTO;
import tn.esprit.matching.clients.SkillClient;
import tn.esprit.matching.clients.UserDto;
import tn.esprit.matching.dto.AdminInvitationDTO;
import tn.esprit.matching.dto.AdminMatchingRowDTO;
import tn.esprit.matching.dto.FreelancerMatchDTO;
import tn.esprit.matching.dto.FreelancerMatchedProjectDTO;
import tn.esprit.matching.entity.Invitation;
import tn.esprit.matching.entity.Matching;
import tn.esprit.matching.repository.InvitationRepository;
import tn.esprit.matching.repository.MatchingRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchingService {

    @Autowired
    private MatchingRepository matchingRepository;

    @Autowired
    private CollectDataService collectDataService;

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private SkillClient skillUserClient;

    @Autowired
    private ProjectClient projectClient;

    @Autowired
    InvitationRepository invitationRepository;

    /**
     * Calcule et retourne les résultats de matching pour un projet donné
     * et une liste de freelances identifiés par leurs IDs.
     *
     * Étapes :
     * 1. Récupérer les UserDto pour enrichir (nom, prénom, etc.).
     * 2. Pour chaque freelancer :
     *    - Charger les données (exp, availability, skills, analyse projet, ...)
     *    - Calculer les scores partiels + score final
     *    - Mettre à jour la table matching
     *    - Construire un FreelancerMatchDTO
     * 3. Retourner les résultats triés par score décroissant.
     */
    public List<FreelancerMatchDTO> getMatchingForProject(
            Long projectId,
            List<Long> freelancerIds,
            String token
    ) {
        if (freelancerIds == null || freelancerIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<FreelancerMatchDTO> result = new ArrayList<>();

        // 1) Récupérer tous les users une seule fois, puis indexer par id
        List<UserDto> allUsers = skillUserClient.getAllUsers(token);
        Map<Long, UserDto> userMap = allUsers.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(UserDto::getId, u -> u, (a, b) -> a));

        for (Long freelancerId : freelancerIds) {
            try {
                UserDto user = userMap.get(freelancerId);
                if (user == null) {
                    System.err.println("Matching: freelancerId " + freelancerId + " introuvable côté user-service, ignoré.");
                    continue;
                }

                // 2) Charger toutes les données nécessaires (async)
                CollectDataService.MatchingDataPackage data =
                        collectDataService.getAllData(freelancerId, projectId).join();

                double availabilityScore = scoreService.scoreAvailability(data.availability());
                double educationScore    = scoreService.scoreEducation(data.education(), data.projectSkills());
                double skillsScore       = scoreService.scoreSkills(data.userSkills(), data.projectSkills());
                double experienceScore   = scoreService.scoreExperience(data.experience(), data.analysis());

                double finalScore = scoreService.calculateFinalScore(
                        skillsScore, experienceScore, educationScore, availabilityScore
                );

                // 2.b) Si vraiment TOUT est à 0 → pratiquement aucune info utile, on ignore
                if (skillsScore == 0 && experienceScore == 0
                        && educationScore == 0 && availabilityScore == 0) {
                    System.err.println("Matching: tous les scores sont nuls pour freelancer "
                            + freelancerId + " sur projet " + projectId + " → ignoré.");
                    continue;
                }

                // 3) Upsert dans la table matching
                upsertMatchingRow(projectId, freelancerId,
                        skillsScore, experienceScore, educationScore, availabilityScore, finalScore);

                // 4) Construire le DTO de réponse
                FreelancerMatchDTO dto = buildFreelancerMatchDTO(
                        freelancerId,
                        user,
                        data,
                        skillsScore,
                        experienceScore,
                        educationScore,
                        availabilityScore,
                        finalScore
                );

                result.add(dto);

            } catch (Exception e) {
                System.err.println("Erreur matching freelancer " + freelancerId
                        + " pour projet " + projectId + " : " + e.getMessage());
                e.printStackTrace();
                // On ignore ce freelancer pour ne pas polluer l'UI avec des cartes vides
            }
        }

        // 5) Trier par score final décroissant
        result.sort(Comparator.comparingDouble(FreelancerMatchDTO::getMatchScore).reversed());
        return result;
    }

    /**
     * Matching automatique :
     * - Si des lignes existent déjà dans 'matching' pour ce projet → on les réutilise (IDs).
     * - Sinon → on récupère tous les freelances depuis le user-service, puis on calcule.
     */
    public List<FreelancerMatchDTO> getMatchingForProjectAuto(Long projectId, String token) {

        // 1) Tenter de réutiliser les matchings déjà calculés pour ce projet
        List<Matching> existingMatchings = matchingRepository.findByProjectId(projectId);

        List<Long> existingFreelancerIds = existingMatchings.stream()
                .map(Matching::getFreelancerId)
                .distinct()
                .collect(Collectors.toList());

        if (!existingFreelancerIds.isEmpty()) {
            System.out.println("DEBUG Matching: " + existingFreelancerIds.size()
                    + " freelances déjà matchés pour le projet " + projectId);
            return getMatchingForProject(projectId, existingFreelancerIds, token);
        }

        // 2) Sinon, on calcule pour TOUS les freelances disponibles

        List<UserDto> allUsers = skillUserClient.getAllUsers(token);
        System.out.println("DEBUG Matching: nb users récupérés du user-service = " + allUsers.size());

        // Ici on pourrait filtrer par rôle FREELANCER si UserDto contient cette info.
        // Exemple (à adapter selon ton modèle) :
        //
        // List<Long> allFreelancerIds = allUsers.stream()
        //     .filter(u -> u.getRoles() != null &&
        //                  u.getRoles().stream().anyMatch(r -> "FREELANCER".equalsIgnoreCase(r)))
        //     .map(UserDto::getId)
        //     .collect(Collectors.toList());
        //
        // Pour l'instant on prend tous les users comme freelances potentiels :
        List<Long> allFreelancerIds = allUsers.stream()
                .map(UserDto::getId)
                .collect(Collectors.toList());

        if (allFreelancerIds.isEmpty()) {
            System.err.println("Aucun freelancer trouvé côté user-service pour le projet " + projectId);
            return Collections.emptyList();
        }

        return getMatchingForProject(projectId, allFreelancerIds, token);
    }

    // ─────────────────────────── PRIVATE HELPERS ───────────────────────────

    private void upsertMatchingRow(
            Long projectId,
            Long freelancerId,
            double skillsScore,
            double experienceScore,
            double educationScore,
            double availabilityScore,
            double finalScore
    ) {
        Matching matching = matchingRepository
                .findByFreelancerIdAndProjectId(freelancerId, projectId);

        if (matching == null) {
            matching = new Matching();
            matching.setFreelancerId(freelancerId);
            matching.setProjectId(projectId);
        }

        matching.setScoreSkills(skillsScore);
        matching.setScoreExperience(experienceScore);
        matching.setScoreEducation(educationScore);
        matching.setScoreAvailability(availabilityScore);
        matching.setScoreFinal(finalScore);
        matching.setStatus("CALCULATED");

        matchingRepository.save(matching);
    }

    private FreelancerMatchDTO buildFreelancerMatchDTO(
            Long freelancerId,
            UserDto user,
            CollectDataService.MatchingDataPackage data,
            double skillsScore,
            double experienceScore,
            double educationScore,
            double availabilityScore,
            double finalScore
    ) {
        // 1) Skills texte
        List<String> skillNames = data.userSkills() != null
                ? data.userSkills().stream()
                .map(s -> s.getSkillName() != null ? s.getSkillName()
                        : s.getCustomSkillName() != null ? s.getCustomSkillName() : "")
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList())
                : new ArrayList<>();

        // 2) Disponibilité textuelle
        String availabilityStatus = (data.availability() != null && data.availability().getStatus() != null)
                ? data.availability().getStatus()
                : "UNKNOWN";

        // 3) "Rôle" : on prend le titre de la première expérience si dispo
        String role = "Freelancer";
        if (data.experience() != null
                && data.experience().getExperiences() != null
                && !data.experience().getExperiences().isEmpty()) {
            String expTitle = data.experience().getExperiences().get(0).getTitle();
            if (expTitle != null && !expTitle.isEmpty()) {
                role = expTitle;
            }
        }

        String firstName = user.getFirstName() != null ? user.getFirstName() : "Unknown";
        String lastName  = user.getLastName()  != null ? user.getLastName()  : "";

        double roundedScore  = Math.round(finalScore * 10.0) / 10.0;
        double ratingOnFive  = Math.round((finalScore / 100.0 * 5.0) * 10.0) / 10.0;

        FreelancerMatchDTO dto = new FreelancerMatchDTO(
                freelancerId,
                firstName,
                lastName,
                role,
                role,
                availabilityStatus,
                skillNames,
                roundedScore
        );

        dto.setRating(ratingOnFive);
        // 4) Stats projets actifs / complétés
        try {
            LocalDate today = LocalDate.now();
            List<ProposalDTO> proposals = projectClient.getProposalsByFreelancer(freelancerId);

            long active = 0;
            long completed = 0;

            for (ProposalDTO p : proposals) {
                LocalDate deadline = p.getDeadline();
                if (deadline != null && deadline.isBefore(today)) {
                    completed++;
                } else {
                    active++;
                }
            }

            dto.setActiveProjects((int) active);
            dto.setCompletedProjects((int) completed);

        } catch (Exception e) {
            System.err.println("Stats non dispo pour user " + freelancerId + " : " + e.getMessage());
            dto.setActiveProjects(0);
            dto.setCompletedProjects(0);
        }

        return dto;
    }

    public List<FreelancerMatchedProjectDTO> getMatchedProjectIdsForFreelancer(Long freelancerId) {
        List<Matching> matchings = matchingRepository.findByFreelancerId(freelancerId);
        if (matchings == null || matchings.isEmpty()) {
            return Collections.emptyList();
        }

        // trier par score décroissant (optionnel)
        matchings.sort(Comparator.comparingDouble(Matching::getScoreFinal).reversed());

        List<FreelancerMatchedProjectDTO> result = new ArrayList<>();
        for (Matching m : matchings) {
            result.add(new FreelancerMatchedProjectDTO(
                    m.getProjectId(),
                    m.getScoreFinal()   // ou *100 si tu veux un pourcentage
            ));
        }
        return result;
    }
    public List<AdminMatchingRowDTO> getAllMatchingsForAdmin() {
        List<Matching> all = matchingRepository.findAllByOrderByProjectIdAscFreelancerIdAsc();
        return all.stream()
                .map(AdminMatchingRowDTO::new)
                .toList();
    }


}