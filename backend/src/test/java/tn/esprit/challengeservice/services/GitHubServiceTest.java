package tn.esprit.challengeservice.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GitHubServiceTest {

    private GitHubService gitHubService;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        gitHubService = new GitHubService();
        ReflectionTestUtils.setField(gitHubService, "githubToken", "gh-token");
        ReflectionTestUtils.setField(gitHubService, "sonarToken", "sonar-token");

        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(gitHubService, "restTemplate");
        server = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @Test
    void doesBranchExist_whenBranchExists_shouldReturnTrue() {
        server.expect(once(), requestTo("https://api.github.com/repos/owner/repo/branches/main"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        boolean exists = gitHubService.doesBranchExist("https://github.com/owner/repo", "main");

        assertTrue(exists);
        server.verify();
    }

    @Test
    void doesBranchExist_whenInvalidInput_shouldReturnFalse() {
        assertFalse(gitHubService.doesBranchExist("", "main"));
        assertFalse(gitHubService.doesBranchExist("https://github.com/owner/repo", ""));
        assertFalse(gitHubService.doesBranchExist("not-a-github-url", "main"));
    }

    @Test
    void doesBranchExist_whenNotFound_shouldReturnFalse() {
        server.expect(once(), requestTo("https://api.github.com/repos/owner/repo/branches/missing"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        boolean exists = gitHubService.doesBranchExist("https://github.com/owner/repo", "missing");

        assertFalse(exists);
        server.verify();
    }

    @Test
    void fetchSonarCloudMetricsByPrUrl_whenValid_shouldReturnMetrics() {
        server.expect(once(), requestTo("https://sonarcloud.io/api/measures/component?component=challenge-org-Freelancy_repo-name&pullRequest=1&metricKeys=bugs,code_smells,vulnerabilities,security_hotspots,coverage,duplicated_lines_density,ncloc,alert_status"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "{\"component\":{\"measures\":[{\"metric\":\"bugs\",\"value\":\"1\"},{\"metric\":\"alert_status\",\"value\":\"OK\"}]}}",
                        MediaType.APPLICATION_JSON
                ));

        Map<String, Object> result = gitHubService.fetchSonarCloudMetricsByPrUrl("https://github.com/owner/repo-name/pull/1");

        assertEquals("1", result.get("bugs"));
        assertEquals("OK", result.get("alert_status"));
        server.verify();
    }

    @Test
    void fetchSonarCloudMetricsByPrUrl_whenInvalid_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> gitHubService.fetchSonarCloudMetricsByPrUrl("invalid-url"));
    }

    @Test
    void doesSonarCloudProjectExist_shouldReturnTrueWhenComponentFound() {
        server.expect(once(), requestTo("https://sonarcloud.io/api/projects/search?organization=challenge-org-freelancy&projects=challenge-org-Freelancy_repo-a"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"components\":[{\"key\":\"challenge-org-Freelancy_repo-a\"}]}", MediaType.APPLICATION_JSON));

        boolean exists = gitHubService.doesSonarCloudProjectExist("repo-a");

        assertTrue(exists);
        server.verify();
    }

    @Test
    void hasRepoSecret_whenNotFound_shouldReturnFalse() {
        server.expect(once(), requestTo("https://api.github.com/repos/challenge-org-Freelancy/repo-a/actions/secrets/SONAR_TOKEN"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        boolean exists = gitHubService.hasRepoSecret("repo-a", "SONAR_TOKEN");

        assertFalse(exists);
        server.verify();
    }

        @Test
        void doesUserExist_shouldHandleSuccessAndNotFound() {
        server.expect(once(), requestTo("https://api.github.com/users/octocat"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess());
        server.expect(once(), requestTo("https://api.github.com/users/missing-user"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertTrue(gitHubService.doesUserExist("octocat"));
        assertFalse(gitHubService.doesUserExist("missing-user"));
        server.verify();
        }

        @Test
        void isCollaboratorAccepted_shouldReturnTrueOnlyFor204() {
        server.expect(once(), requestTo("https://api.github.com/repos/challenge-org-Freelancy/repo-a/collaborators/alice"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.NO_CONTENT));
        server.expect(once(), requestTo("https://api.github.com/repos/challenge-org-Freelancy/repo-a/collaborators/bob"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertTrue(gitHubService.isCollaboratorAccepted("repo-a", "alice"));
        assertFalse(gitHubService.isCollaboratorAccepted("repo-a", "bob"));
        server.verify();
        }

        @Test
        void getLatestPullRequestNumber_shouldReturnLatestAndThrowOnEmpty() {
        server.expect(once(), requestTo("https://api.github.com/repos/challenge-org-Freelancy/repo-a/pulls?state=all&sort=created&direction=desc&per_page=1"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("[{\"number\":42}]", MediaType.APPLICATION_JSON));

        String number = gitHubService.getLatestPullRequestNumber("repo-a");
        assertEquals("42", number);

        server.verify();

        MockRestServiceServer secondServer = MockRestServiceServer.bindTo((RestTemplate) ReflectionTestUtils.getField(gitHubService, "restTemplate")).build();
        secondServer.expect(once(), requestTo("https://api.github.com/repos/challenge-org-Freelancy/repo-b/pulls?state=all&sort=created&direction=desc&per_page=1"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        assertThrows(RuntimeException.class, () -> gitHubService.getLatestPullRequestNumber("repo-b"));
        secondServer.verify();
        }

        @Test
        void fetchSonarCloudMetrics_whenBadStatus_shouldThrow() {
        server.expect(once(), requestTo("https://sonarcloud.io/api/measures/component?component=challenge-org-Freelancy_repo-a&pullRequest=3&metricKeys=bugs,code_smells,vulnerabilities,security_hotspots,coverage,duplicated_lines_density,ncloc,alert_status"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertThrows(RuntimeException.class, () -> gitHubService.fetchSonarCloudMetrics("repo-a", "3"));
        server.verify();
        }

        @Test
        void doesSonarCloudProjectExist_shouldReturnFalseForMissingComponents() {
        server.expect(once(), requestTo("https://sonarcloud.io/api/projects/search?organization=challenge-org-freelancy&projects=challenge-org-Freelancy_repo-x"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        boolean exists = gitHubService.doesSonarCloudProjectExist("repo-x");

        assertFalse(exists);
        server.verify();
        }

        @Test
        void doesSonarCloudProjectExist_shouldReturnFalseOnHttpError() {
        server.expect(once(), requestTo("https://sonarcloud.io/api/projects/search?organization=challenge-org-freelancy&projects=challenge-org-Freelancy_repo-y"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        boolean exists = gitHubService.doesSonarCloudProjectExist("repo-y");

        assertFalse(exists);
        server.verify();
        }

        @Test
        void createSonarCloudProject_whenTokenMissing_shouldSkip() {
        ReflectionTestUtils.setField(gitHubService, "sonarToken", "");

        gitHubService.createSonarCloudProject("repo-skip");

        server.verify();
        }

        @Test
        void createSonarCloudProject_whenAlreadyExists_shouldNotThrow() {
        server.expect(once(), requestTo("https://sonarcloud.io/api/projects/create?name=repo-a&organization=challenge-org-freelancy&project=challenge-org-Freelancy_repo-a"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                .body("{\"errors\":[{\"msg\":\"Project already exists\"}]}"));

        gitHubService.createSonarCloudProject("repo-a");

        server.verify();
        }

        @Test
        void createPullRequest_whenValidationError_shouldThrowFriendlyMessage() {
        server.expect(once(), requestTo("https://api.github.com/repos/challenge-org-Freelancy/repo-a"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("{\"default_branch\":\"main\"}", MediaType.APPLICATION_JSON));
        server.expect(once(), requestTo("https://api.github.com/repos/challenge-org-Freelancy/repo-a/pulls"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"message\":\"A pull request already exists\"}"));

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> gitHubService.createPullRequest("repo-a", "feature"));

        assertTrue(ex.getMessage().contains("already exists"));
        server.verify();
        }

        @Test
        void ensureSonarSetupOrThrow_whenTokenMissing_shouldThrow() {
        ReflectionTestUtils.setField(gitHubService, "sonarToken", "");

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> gitHubService.ensureSonarSetupOrThrow("repo-a"));

        assertTrue(ex.getMessage().contains("SONAR_TOKEN"));
        }

        @Test
        void parsePrUrl_privateMethod_shouldHandleInvalid() {
        Object result = ReflectionTestUtils.invokeMethod(gitHubService, "parsePrUrl", "https://example.com/x");
        assertNull(result);
        }
}
