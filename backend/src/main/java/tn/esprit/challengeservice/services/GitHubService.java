package tn.esprit.challengeservice.services;

import com.goterl.lazysodium.SodiumJava;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Service
public class GitHubService {

    private static final String GITHUB_API_BASE = "https://api.github.com";
    private static final String TEMPLATE_URL =
            "https://api.github.com/repos/challenge-org-Freelancy/challenge-Test/generate";
    private static final String ORG_OWNER = "challenge-org-Freelancy";
    private static final String SONAR_ORG = "challenge-org-freelancy";
    private static final int SEAL_BYTES = 48;
    private static final Pattern SAFE_PATH_SEGMENT = Pattern.compile("^[A-Za-z0-9._-]+$");
    private static final Pattern SAFE_BRANCH_NAME = Pattern.compile("^[A-Za-z0-9._/-]+$");

    @Value("${github.token}")
    private String githubToken;

    @Value("${sonar.token}")
    private String sonarToken;

    private final RestTemplate restTemplate = new RestTemplate();

    public String createRepository(String repoName) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubToken);
        headers.set("Accept", "application/vnd.github+json");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "owner", ORG_OWNER,
                "name", repoName,
                "private", true
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                TEMPLATE_URL,
                HttpMethod.POST,
                request,
                Map.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            String htmlUrl = (String) response.getBody().get("html_url");
            log.info("GitHub repository created successfully: {}", htmlUrl);
            return htmlUrl;
        }

        throw new RuntimeException("Failed to create GitHub repository: " + response.getStatusCode());
    }

    public void addCollaborator(String repoName, String usernameGithub) {
        String safeRepoName = requireSafePathSegment(repoName, "repoName");
        String safeUsername = requireSafePathSegment(usernameGithub, "usernameGithub");
        String url = buildGithubUrl("repos", ORG_OWNER, safeRepoName, "collaborators", safeUsername);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubToken);
        headers.set("Accept", "application/vnd.github+json");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of("permission", "push");

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    request,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Collaborator {} added to repo {}", usernameGithub, repoName);
            } else {
                throw new RuntimeException("Failed to add collaborator: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException(
                    "GitHub collaborator invite failed (404). Verify repo '" + repoName + "' exists under org '"
                            + ORG_OWNER + "' and user '" + usernameGithub + "' exists.",
                    e
            );
        }
    }

    /**
     * Checks if a branch exists in a GitHub repository.
     * @param repoUrl Full repo URL (e.g. https://github.com/challenge-org-Freelancy/repo-name or .../repo-name.git)
     * @param branchName Branch name to check
     * @return true if branch exists, false otherwise
     */
    public boolean doesBranchExist(String repoUrl, String branchName) {
        if (repoUrl == null || repoUrl.isBlank() || branchName == null || branchName.isBlank()) {
            return false;
        }
        String ownerRepo = extractOwnerRepoFromUrl(repoUrl);
        if (ownerRepo == null) {
            return false;
        }
        String[] ownerRepoParts = ownerRepo.split("/");
        if (ownerRepoParts.length != 2 || !SAFE_BRANCH_NAME.matcher(branchName.trim()).matches()) {
            return false;
        }
        String url = buildGithubUrl("repos", ownerRepoParts[0], ownerRepoParts[1], "branches", branchName.trim());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubToken);
        headers.set("Accept", "application/vnd.github+json");

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

    private String extractOwnerRepoFromUrl(String repoUrl) {
        try {
            String normalized = repoUrl.trim();
            if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
                normalized = "https://" + normalized;
            }

            URI uri = URI.create(normalized);
            if (uri.getHost() == null || !uri.getHost().toLowerCase(Locale.ROOT).contains("github.com")) {
                return null;
            }

            String path = uri.getPath();
            if (path == null || path.isBlank()) {
                return null;
            }

            String[] parts = Arrays.stream(path.split("/"))
                    .filter(s -> !s.isBlank())
                    .toArray(String[]::new);

            if (parts.length < 2) {
                return null;
            }

            String owner = parts[0];
            String repo = parts[1].replaceAll("\\.git$", "");
            if (SAFE_PATH_SEGMENT.matcher(owner).matches() && SAFE_PATH_SEGMENT.matcher(repo).matches()) {
                return owner + "/" + repo;
            }
            return null;
        } catch (Exception e) {
            log.warn("Failed to extract owner/repo from url: {}", repoUrl);
            return null;
        }
    }

    public boolean doesUserExist(String usernameGithub) {
        if (usernameGithub == null || usernameGithub.isBlank()) {
            return false;
        }

        String safeUsername = usernameGithub.trim();
        if (!SAFE_PATH_SEGMENT.matcher(safeUsername).matches()) {
            return false;
        }

        String url = buildGithubUrl("users", safeUsername);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.github+json");

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.GET, request, Void.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

    public boolean isCollaboratorAccepted(String repoName, String usernameGithub) {
        String safeRepoName = requireSafePathSegment(repoName, "repoName");
        String safeUsername = requireSafePathSegment(usernameGithub, "usernameGithub");
        String url = buildGithubUrl("repos", ORG_OWNER, safeRepoName, "collaborators", safeUsername);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubToken);
        headers.set("Accept", "application/vnd.github+json");

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    Void.class
            );
            return response.getStatusCode().value() == 204;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

    public void createSonarCloudProject(String repoName) {
        if (sonarToken == null || sonarToken.isEmpty()) {
            log.warn("SONAR_TOKEN not configured, skipping SonarCloud project creation for repo: {}", repoName);
            return;
        }

        String projectKey = toSonarProjectKey(repoName);
        String url = UriComponentsBuilder.fromHttpUrl("https://sonarcloud.io/api/projects/create")
                .queryParam("name", repoName)
                .queryParam("organization", SONAR_ORG)
                .queryParam("project", projectKey)
                .toUriString();

        HttpHeaders headers = buildSonarHeaders();

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("SonarCloud project created: {}", projectKey);
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 400 && e.getResponseBodyAsString().contains("already exists")) {
                log.info("SonarCloud project already exists: {}", projectKey);
            } else {
                log.error("Failed to create SonarCloud project {}: status={}, body={}",
                        projectKey, e.getStatusCode(), e.getResponseBodyAsString());
                throw new RuntimeException("Failed to create SonarCloud project: status="
                        + e.getStatusCode().value() + ", body=" + e.getResponseBodyAsString());
            }
        }
    }

    public void ensureSonarSetupOrThrow(String repoName) {
        if (sonarToken == null || sonarToken.isBlank()) {
            throw new RuntimeException("SONAR_TOKEN is not configured on challenge-service.");
        }

        addSonarTokenSecret(repoName);
        if (!hasRepoSecret(repoName, "SONAR_TOKEN")) {
            throw new RuntimeException("SONAR_TOKEN secret was not added to GitHub repo " + repoName);
        }

        createSonarCloudProject(repoName);
        if (!doesSonarCloudProjectExist(repoName)) {
            throw new RuntimeException("SonarCloud project was not created/found for key " + toSonarProjectKey(repoName));
        }
    }

    public String createPullRequest(String repoName, String branchName) {
        String safeRepoName = requireSafePathSegment(repoName, "repoName");
        String safeBranchName = requireSafeBranchName(branchName);
        String url = buildGithubUrl("repos", ORG_OWNER, safeRepoName, "pulls");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubToken);
        headers.set("Accept", "application/vnd.github+json");
        headers.setContentType(MediaType.APPLICATION_JSON);

        String baseBranch = getDefaultBranch(repoName);

        Map<String, Object> body = Map.of(
                "title", "Challenge Submission",
            "head", safeBranchName,
                "base", baseBranch,
            "body", "Automated challenge submission from branch " + safeBranchName
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            if (githubToken == null || githubToken.isBlank()) {
                throw new RuntimeException("GitHub token is not configured. Set GITHUB_TOKEN environment variable.");
            }
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String prUrl = (String) response.getBody().get("html_url");
                log.info("Pull request created for repo {}: {}", repoName, prUrl);
                return prUrl;
            }
            throw new RuntimeException("Failed to create pull request: " + response.getStatusCode());
        } catch (HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            int status = e.getStatusCode().value();
            if (status == 401) {
                throw new RuntimeException("Invalid or missing GitHub token. Check GITHUB_TOKEN is set correctly.");
            }
            if (status == 404) {
                throw new RuntimeException("Repository or branch not found. Ensure the branch '" + safeBranchName + "' exists and is pushed.");
            }
            if (status == 422) {
                log.warn("GitHub 422 creating PR for repo {} branch {}: {}", safeRepoName, safeBranchName, errorBody);
                if (errorBody != null && errorBody.contains("A pull request already exists")) {
                    throw new RuntimeException("A pull request already exists for branch '" + safeBranchName + "'. Check GitHub.");
                }
                if (errorBody != null && errorBody.contains("Reference does not exist")) {
                    throw new RuntimeException("Branch '" + safeBranchName + "' not found. Ensure it exists and is pushed to the remote.");
                }
                throw new RuntimeException("Cannot create pull request: " + (errorBody != null && errorBody.length() < 200 ? errorBody : "Validation failed. Check branch name and that it is pushed."));
            }
            throw new RuntimeException("Failed to create pull request: " + (errorBody != null && !errorBody.isBlank() ? errorBody : e.getMessage()));
        }
    }

    private String getDefaultBranch(String repoName) {
        String safeRepoName = requireSafePathSegment(repoName, "repoName");
        String url = buildGithubUrl("repos", ORG_OWNER, safeRepoName);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubToken);
        headers.set("Accept", "application/vnd.github+json");
        HttpEntity<Void> request = new HttpEntity<>(headers);
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String defaultBranch = (String) response.getBody().get("default_branch");
                return defaultBranch != null && !defaultBranch.isBlank() ? defaultBranch : "main";
            }
        } catch (Exception e) {
            log.warn("Could not fetch default branch for repo {}, using main: {}", repoName, e.getMessage());
        }
        return "main";
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> fetchSonarCloudMetrics(String repoName, String pullRequestKey) {
        String projectKey = toSonarProjectKey(repoName);
        String url = "https://sonarcloud.io/api/measures/component"
                + "?component=" + projectKey
                + "&pullRequest=" + pullRequestKey
                + "&metricKeys=bugs,code_smells,vulnerabilities,security_hotspots,coverage,duplicated_lines_density,ncloc,alert_status";

        HttpHeaders headers = buildSonarHeaders();

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to fetch SonarCloud metrics for: " + projectKey);
        }

        Map<String, Object> component = (Map<String, Object>) response.getBody().get("component");
        List<Map<String, String>> measures = (List<Map<String, String>>) component.get("measures");

        Map<String, Object> result = new HashMap<>();
        for (Map<String, String> measure : measures) {
            result.put(measure.get("metric"), measure.get("value"));
        }

        log.info("Fetched SonarCloud metrics for project {}, PR {}: {}", projectKey, pullRequestKey, result);
        return result;
    }

    /**
     * Fetches SonarCloud metrics for a GitHub pull request URL.
     * Example: https://github.com/challenge-org-Freelancy/AI-Task-Manager-with-Smart-Suggestions-Ameny323/pull/1
     */
    public Map<String, Object> fetchSonarCloudMetricsByPrUrl(String prUrl) {
        var parsed = parsePrUrl(prUrl);
        if (parsed == null) {
            throw new IllegalArgumentException("Invalid GitHub PR URL. Expected format: https://github.com/owner/repo/pull/number");
        }
        String repoName = parsed.get("repoName");
        String pullNumber = parsed.get("pullNumber");
        return fetchSonarCloudMetrics(repoName, pullNumber);
    }

    private Map<String, String> parsePrUrl(String prUrl) {
        if (prUrl == null || !prUrl.contains("github.com/")) {
            return null;
        }
        try {
            String path = prUrl.trim().replace(".git", "");
            int idx = path.indexOf("github.com/");
            if (idx < 0) return null;
            path = path.substring(idx + 11);
            if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
            String[] parts = path.split("/");
            if (parts.length >= 4 && "pull".equalsIgnoreCase(parts[2])) {
                String repoName = parts[1];
                String pullNumber = parts[3];
                return Map.of("repoName", repoName, "pullNumber", pullNumber);
            }
        } catch (Exception e) {
            log.warn("Failed to parse PR URL: {}", prUrl);
        }
        return null;
    }

    public String getLatestPullRequestNumber(String repoName) {
        String safeRepoName = requireSafePathSegment(repoName, "repoName");
        String url = UriComponentsBuilder.fromHttpUrl(buildGithubUrl("repos", ORG_OWNER, safeRepoName, "pulls"))
            .queryParam("state", "all")
            .queryParam("sort", "created")
            .queryParam("direction", "desc")
            .queryParam("per_page", 1)
            .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubToken);
        headers.set("Accept", "application/vnd.github+json");

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, request, List.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && !response.getBody().isEmpty()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> pr = (Map<String, Object>) response.getBody().get(0);
            return String.valueOf(pr.get("number"));
        }

        throw new RuntimeException("No pull requests found for repo: " + repoName);
    }

    public void addSonarTokenSecret(String repoName) {
        if (sonarToken == null || sonarToken.isEmpty()) {
            log.warn("SONAR_TOKEN not configured, skipping secret creation for repo: {}", repoName);
            return;
        }
        addRepoSecret(repoName, "SONAR_TOKEN", sonarToken);
    }

    public boolean hasRepoSecret(String repoName, String secretName) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubToken);
        headers.set("Accept", "application/vnd.github+json");

        String safeRepoName = requireSafePathSegment(repoName, "repoName");
        String safeSecretName = requireSafePathSegment(secretName, "secretName");
        String url = buildGithubUrl("repos", ORG_OWNER, safeRepoName, "actions", "secrets", safeSecretName);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public boolean doesSonarCloudProjectExist(String repoName) {
        String projectKey = toSonarProjectKey(repoName);
        String url = UriComponentsBuilder.fromHttpUrl("https://sonarcloud.io/api/projects/search")
                .queryParam("organization", SONAR_ORG)
                .queryParam("projects", projectKey)
                .toUriString();

        HttpEntity<Void> request = new HttpEntity<>(buildSonarHeaders());

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return false;
            }

            List<Map<String, Object>> components = (List<Map<String, Object>>) response.getBody().get("components");
            if (components == null) {
                return false;
            }

            return components.stream().anyMatch(c -> projectKey.equals(c.get("key")));
        } catch (HttpClientErrorException e) {
            log.warn("Failed checking SonarCloud project {}: status={}, body={}",
                    projectKey, e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        }
    }

    private HttpHeaders buildSonarHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(sonarToken, "");
        headers.set("Accept", "application/json");
        return headers;
    }

    private String toSonarProjectKey(String repoName) {
        return ORG_OWNER + "_" + repoName;
    }

    private void addRepoSecret(String repoName, String secretName, String secretValue) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubToken);
        headers.set("Accept", "application/vnd.github+json");
        headers.setContentType(MediaType.APPLICATION_JSON);

        String safeRepoName = requireSafePathSegment(repoName, "repoName");
        String safeSecretName = requireSafePathSegment(secretName, "secretName");

        String keyUrl = buildGithubUrl("repos", ORG_OWNER, safeRepoName, "actions", "secrets", "public-key");
        HttpEntity<Void> keyRequest = new HttpEntity<>(headers);
        ResponseEntity<Map> keyResponse = restTemplate.exchange(keyUrl, HttpMethod.GET, keyRequest, Map.class);

        if (!keyResponse.getStatusCode().is2xxSuccessful() || keyResponse.getBody() == null) {
            throw new RuntimeException("Failed to get repo public key for: " + safeRepoName);
        }

        String publicKeyBase64 = (String) keyResponse.getBody().get("key");
        String keyId = (String) keyResponse.getBody().get("key_id");

        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
        byte[] messageBytes = secretValue.getBytes(StandardCharsets.UTF_8);
        byte[] cipherText = new byte[SEAL_BYTES + messageBytes.length];

        SodiumJava sodium = new SodiumJava();
        int result = sodium.crypto_box_seal(cipherText, messageBytes, messageBytes.length, publicKeyBytes);
        if (result != 0) {
            throw new RuntimeException("Failed to encrypt secret value");
        }

        String encryptedValue = Base64.getEncoder().encodeToString(cipherText);

        String secretUrl = buildGithubUrl("repos", ORG_OWNER, safeRepoName, "actions", "secrets", safeSecretName);
        Map<String, String> body = Map.of(
                "encrypted_value", encryptedValue,
                "key_id", keyId
        );
        HttpEntity<Map<String, String>> secretRequest = new HttpEntity<>(body, headers);
        ResponseEntity<Void> response = restTemplate.exchange(secretUrl, HttpMethod.PUT, secretRequest, Void.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Secret {} added to repo {}", safeSecretName, safeRepoName);
        } else {
            throw new RuntimeException("Failed to add secret to repo: " + response.getStatusCode());
        }
    }

    private String buildGithubUrl(String... pathSegments) {
        return UriComponentsBuilder.fromHttpUrl(GITHUB_API_BASE)
                .pathSegment(pathSegments)
                .toUriString();
    }

    private String requireSafePathSegment(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        String normalized = value.trim();
        if (!SAFE_PATH_SEGMENT.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid " + fieldName + " format");
        }
        return normalized;
    }

    private String requireSafeBranchName(String branchName) {
        if (branchName == null || branchName.isBlank()) {
            throw new IllegalArgumentException("branchName cannot be blank");
        }
        String normalized = branchName.trim();
        if (!SAFE_BRANCH_NAME.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid branchName format");
        }
        return normalized;
    }
}
