# Frontend Guide: Sonar Analysis by GitHub PR URL

## Overview

The backend exposes an endpoint to fetch SonarCloud results directly from a GitHub pull request URL. Use this for the analysis page when you have the PR URL (e.g. after submit) instead of a participation ID.

---

## Endpoint

| Method | Path | Description |
|--------|------|-------------|
| GET | `/participations/sonar-results/fetch-by-url` | Fetch SonarCloud metrics by GitHub PR URL |

### Query Parameter

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `prUrl` | string | Yes | Full GitHub PR URL (e.g. `https://github.com/challenge-org-Freelancy/repo-name/pull/1`) |

---

## Request Example

```
GET {apiUrl}/participations/sonar-results/fetch-by-url?prUrl=https://github.com/challenge-org-Freelancy/AI-Task-Manager-with-Smart-Suggestions-Ameny323/pull/1
```

---

## Response Format

```json
{
  "pullRequestUrl": "https://github.com/challenge-org-Freelancy/AI-Task-Manager-with-Smart-Suggestions-Ameny323/pull/1",
  "qualityGateStatus": "OK",
  "bugs": 0,
  "codeSmells": 5,
  "vulnerabilities": 0,
  "securityHotspots": 2,
  "coverage": 85.5,
  "duplication": 2.1,
  "linesOfCode": 1500,
  "rawMetrics": {
    "alert_status": "OK",
    "bugs": "0",
    "code_smells": "5",
    "vulnerabilities": "0",
    "security_hotspots": "2",
    "coverage": "85.5",
    "duplicated_lines_density": "2.1",
    "ncloc": "1500"
  }
}
```

| Field | Type | Description |
|-------|------|-------------|
| `pullRequestUrl` | string | The PR URL used for the request |
| `qualityGateStatus` | string | `"OK"` or `"ERROR"` |
| `bugs` | number | Number of bugs |
| `codeSmells` | number | Number of code smells |
| `vulnerabilities` | number | Number of vulnerabilities |
| `securityHotspots` | number | Number of security hotspots |
| `coverage` | number | Test coverage percentage |
| `duplication` | number | Code duplication percentage |
| `linesOfCode` | number | Lines of code |
| `rawMetrics` | object | Raw SonarCloud metrics |

---

## Angular Service Method

Add this to your `ParticipationService` (or equivalent):

```typescript
getSonarResultsByPrUrl(prUrl: string): Observable<SonarResultByUrlResponse> {
  const params = new HttpParams().set('prUrl', prUrl);
  return this.http.get<SonarResultByUrlResponse>(
    `${this.baseUrl}/sonar-results/fetch-by-url`,
    { params }
  );
}

export interface SonarResultByUrlResponse {
  pullRequestUrl: string;
  qualityGateStatus: string;
  bugs: number;
  codeSmells: number;
  vulnerabilities: number;
  securityHotspots: number;
  coverage: number;
  duplication: number;
  linesOfCode: number;
  rawMetrics: Record<string, string | number>;
}
```

**Note:** `baseUrl` should already point to `/participations` (e.g. `${environment.apiUrl}/participations`). If your base is just the API root, use `${environment.apiUrl}/participations/sonar-results/fetch-by-url`.

---

## When to Use This Endpoint

| Scenario | Use |
|----------|-----|
| User has **participation ID** (from join/submit flow) | `GET /participations/{id}/sonar-results` or `.../sonar-results/status` |
| User has **PR URL** (e.g. from submit response, or direct link) | `GET /participations/sonar-results/fetch-by-url?prUrl=...` |

---

## Integration Flow

### Option A: After Submit (you have PR URL)

When the user submits a challenge, the backend returns `pullRequestUrl`. You can:

1. Navigate to the analysis page with the PR URL (e.g. as query param: `/challenges/analyse?prUrl=...`)
2. On load, call `getSonarResultsByPrUrl(prUrl)` instead of `getSonarResults(participationId)`
3. Map the response to your `AnalysisData` / `SonarResultResponse` format and display

### Option B: Analysis Page Supports Both

```typescript
ngOnInit(): void {
  this.route.queryParams.subscribe(params => {
    const prUrl = params['prUrl'];
    const participationId = params['participationId'];

    if (prUrl) {
      this.loadByPrUrl(prUrl);
    } else if (participationId) {
      this.loadByParticipationId(participationId);
    } else {
      this.errorMessage = 'Missing participation ID or PR URL';
    }
  });
}

private loadByPrUrl(prUrl: string): void {
  this.isLoading = true;
  this.participationService.getSonarResultsByPrUrl(prUrl).subscribe({
    next: (res) => {
      this.data = this.mapUrlResponseToAnalysisData(res);
      this.isLoading = false;
    },
    error: (err) => {
      this.errorMessage = 'Failed to load analysis. The SonarCloud analysis may not be ready yet.';
      this.isLoading = false;
    }
  });
}

private mapUrlResponseToAnalysisData(res: SonarResultByUrlResponse): AnalysisData {
  // Map qualityGateStatus, bugs, codeSmells, etc. to your AnalysisData structure
  // Same mapping logic as for SonarResultResponse, but using res.* instead of result.*
  return {
    qualityGateStatus: res.qualityGateStatus === 'OK' ? 'PASSED' : 'FAILED',
    codeHealthScore: this.calculateHealthScore(res),
    metrics: { /* ... map from res */ },
    coverage: res.coverage,
    duplication: res.duplication,
    codebaseInfo: [
      { label: 'Lines of Code', value: res.linesOfCode.toLocaleString() },
      { label: 'Pull Request', value: res.pullRequestUrl }
    ],
    detailedBreakdown: [ /* ... */ ]
  };
}
```

---

## Error Handling

- **404 / 500**: SonarCloud analysis may not be ready yet, or the repo/PR is not configured in SonarCloud.
- **400**: Invalid PR URL format. Expected: `https://github.com/owner/repo/pull/number`.

For “analysis not ready” cases, you can implement polling: retry `getSonarResultsByPrUrl` every 10 seconds until success or max attempts.

---

## CORS

The backend allows `http://localhost:4200` by default. If your frontend runs on a different origin, ensure CORS is configured on the backend.
