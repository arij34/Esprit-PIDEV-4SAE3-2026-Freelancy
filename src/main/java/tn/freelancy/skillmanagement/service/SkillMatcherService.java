package tn.freelancy.skillmanagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.freelancy.skillmanagement.dto.SkillMatchResult;
import tn.freelancy.skillmanagement.entity.*;
import tn.freelancy.skillmanagement.repository.PendingSkillRepository;
import tn.freelancy.skillmanagement.repository.SkillRepository;

import java.util.*;

@Service
public class SkillMatcherService {

    @Autowired
    private SkillRepository skillRepository;
    @Autowired
    private PendingSkillRepository pendingSkillRepository;

    // ✅ TEMP USER ID FOR TESTING
    private static final Long TEST_USER_ID = 1L; // ⚠️ change later when integrating security
    // =========================
    // Alias Dictionary (NORMALIZED)
    // =========================
    private static final Map<String, String> ALIASES = new HashMap<>();

    static {

        // JavaScript
        putAlias("js", "javascript");
        putAlias("javascript", "javascript");
        putAlias("js.js", "javascript");

        // TypeScript
        putAlias("ts", "typescript");
        putAlias("typescript", "typescript");

        // Python
        putAlias("python", "python");
        putAlias("py", "python");
        putAlias("python3", "python");

        // Java
        putAlias("java", "java");
        putAlias("jdk", "java");

        // Spring Boot
        putAlias("spring", "springboot");
        putAlias("springboot", "springboot");
        putAlias("spring boot", "springboot");

        // Node
        putAlias("node", "nodejs");
        putAlias("nodejs", "nodejs");
        putAlias("node.js", "nodejs");

        // React
        putAlias("react", "react");
        putAlias("reactjs", "react");
        putAlias("react.js", "react");

        // Vue
        putAlias("vue", "vuejs");
        putAlias("vuejs", "vuejs");
        putAlias("vue.js", "vuejs");

        // Angular
        putAlias("angular", "angular");
        putAlias("angularjs", "angular");

        // Databases
        putAlias("mysql", "mysql");
        putAlias("postgres", "postgresql");
        putAlias("postgresql", "postgresql");
        putAlias("mongo", "mongodb");
        putAlias("mongodb", "mongodb");

        // DevOps
        putAlias("k8s", "kubernetes");
        putAlias("kube", "kubernetes");
        putAlias("kubernetes", "kubernetes");
        putAlias("docker", "docker");
        putAlias("git", "git");
        putAlias("github", "git");
        putAlias("aws", "aws");
        putAlias("amazon", "aws");

        // C
        putAlias("c#", "csharp");
        putAlias("csharp", "csharp");
        putAlias("c++", "cplusplus");
        putAlias("cpp", "cplusplus");

        // PHP
        putAlias("php", "php");
        putAlias("laravel", "laravel");
        putAlias("symfony", "php");
    }

    private static void putAlias(String key, String value) {
        ALIASES.put(normalizeStatic(key), normalizeStatic(value));
    }

    private static String normalizeStatic(String input) {
        return input.toLowerCase()
                .replaceAll("[^a-z0-9]", "")
                .trim();
    }

    // ====================================================
    // MAIN MATCHING METHOD
    // ====================================================
    public SkillMatchResult findMatchingSkill(String userInput) {

        if (userInput == null || userInput.isBlank()) {
            return null;
        }

        String cleaned = normalize(userInput);

        // 1️⃣ Alias match
        String aliasMatch = ALIASES.get(cleaned);
        if (aliasMatch != null) {

            Skill skill = skillRepository
                    .findByNormalizedNameIgnoreCase(aliasMatch);

            if (skill != null) {
                return new SkillMatchResult(skill, 1.0, true, false);
            }
        }

        // 2️⃣ Exact match
        Skill exact = skillRepository
                .findByNormalizedNameIgnoreCase(cleaned);

        if (exact != null) {
            return new SkillMatchResult(exact, 1.0, true, false);
        }

        // 3️⃣ Fuzzy match
        List<Skill> candidates = skillRepository.findAll();

        Skill bestMatch = null;
        double bestScore = 0.0;

        for (Skill skill : candidates) {

            String normalizedSkill = normalize(skill.getNormalizedName());
            double similarity = calculateSimilarity(cleaned, normalizedSkill);

            if (similarity > bestScore) {
                bestScore = similarity;
                bestMatch = skill;
            }
        }

        if (bestMatch != null) {

            if (bestScore >= 0.75) {
                return new SkillMatchResult(bestMatch, bestScore, false, false);
            }

            if (bestScore >= 0.55) {
                return new SkillMatchResult(bestMatch, bestScore, false, true);
            }
        }

        // 4️⃣ No match → create PendingSkill
        createPendingSkill(userInput, cleaned, TEST_USER_ID);

        return null;
    }

    // ====================================================
    // LEVENSHTEIN DISTANCE
    // ====================================================
    private int levenshteinDistance(String a, String b) {

        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {

                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(
                            Math.min(
                                    dp[i - 1][j] + 1,
                                    dp[i][j - 1] + 1
                            ),
                            dp[i - 1][j - 1] +
                                    (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1)
                    );
                }
            }
        }

        return dp[a.length()][b.length()];
    }

    // ====================================================
    // NORMALIZATION
    // ====================================================
    private String normalize(String input) {
        return input.toLowerCase()
                .replaceAll("[^a-z0-9]", "")
                .trim();
    }

    // ====================================================
    // SIMILARITY CALCULATION
    // ====================================================
    private double calculateSimilarity(String input, String skill) {

        if (input.equals(skill)) {
            return 1.0;
        }

        if (skill.contains(input)) {
            return 0.90;
        }

        if (input.contains(skill)) {
            return 0.85;
        }

        if (skill.startsWith(input)) {
            return 0.88;
        }

        int distance = levenshteinDistance(input, skill);
        int maxLength = Math.max(input.length(), skill.length());

        if (maxLength == 0) {
            return 0;
        }

        double levScore = 1.0 - ((double) distance / maxLength);

        if (distance == 1) {
            levScore += 0.1;
        }

        return levScore;
    }
    private void createPendingSkill(String originalInput,
                                    String normalized,
                                    Long userId) {

        boolean alreadyExists =
                pendingSkillRepository
                        .existsByNormalizedNameAndStatus(normalized, Status.DRAFT);

        if (alreadyExists) {
            return;
        }

        PendingSkill pendingSkill = new PendingSkill(
                originalInput,
                normalized,
                userId,
                Source.FREELANCER,
                Status.DRAFT
        );

        pendingSkillRepository.save(pendingSkill);
    }
}