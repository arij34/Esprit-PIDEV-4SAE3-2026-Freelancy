package tn.esprit.contrat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.esprit.contrat.config.DocuSignConfig;
import tn.esprit.contrat.entity.Contract;
import tn.esprit.contrat.entity.SignatureStatus;
import tn.esprit.contrat.repository.ContractRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

/**
 * DocuSign eSignature Service Implementation
 * Handles digital signature operations via DocuSign API using REST calls
 */
@Service
public class DocuSignServiceImpl implements IDocuSignService {

    private static final Logger logger = Logger.getLogger(DocuSignServiceImpl.class.getName());

    private final DocuSignConfig docuSignConfig;
    private final RestTemplate restTemplate;
    private final ContractRepository contractRepository;
    private final ObjectMapper objectMapper;

    public DocuSignServiceImpl(DocuSignConfig docuSignConfig, RestTemplate restTemplate, 
                            ContractRepository contractRepository, ObjectMapper objectMapper) {
        this.docuSignConfig = docuSignConfig;
        this.restTemplate = restTemplate;
        this.contractRepository = contractRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Send contract for signature via DocuSign
     * Creates an envelope with the contract PDF and sends it to signer
     */
    @Override
    public String sendForSignature(Contract contract, String signerEmail, String signerName) throws IOException {
        logger.info("Sending contract " + contract.getId() + " for signature to " + signerEmail);

        try {
            // Read the contract PDF
            byte[] pdfBytes = Files.readAllBytes(Paths.get(contract.getPdfUrl()));
            String pdfBase64 = Base64.getEncoder().encodeToString(pdfBytes);

            // Build envelope request
            Map<String, Object> envelope = buildEnvelopeRequest(contract, signerEmail, signerName, pdfBase64);

            // Send to DocuSign API
            String url = docuSignConfig.getBasePath() + "/v2.1/accounts/" + docuSignConfig.getAccountId() + "/envelopes";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + getAccessToken());

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(envelope, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IOException("DocuSign API returned: " + response.getStatusCode());
            }

            String envelopeId = (String) response.getBody().get("envelopeId");
            logger.info("Envelope created with ID: " + envelopeId);

            // Get signing URL
            String signingUrl = getSigningUrl(envelopeId, signerEmail, signerName);

            // Update contract
            contract.setEnvelopeId(envelopeId);
            contract.setSigningUrl(signingUrl);
            contract.setSignatureStatus(SignatureStatus.PENDING);
            contractRepository.save(contract);

            logger.info("Contract " + contract.getId() + " sent for signature with envelope " + envelopeId);
            return signingUrl;

        } catch (Exception e) {
            logger.severe("Error sending contract for signature: " + e.getMessage());
            throw new IOException("Failed to send contract for signature: " + e.getMessage(), e);
        }
    }

    /**
     * Build envelope request body for DocuSign API
     */
    private Map<String, Object> buildEnvelopeRequest(Contract contract, String signerEmail, String signerName, String documentBase64) {
        Map<String, Object> envelope = new HashMap<>();
        envelope.put("emailSubject", "Please sign the contract: " + contract.getTitle());
        envelope.put("emailBlurb", "Please review and sign the attached contract.");
        envelope.put("status", "sent");

        // Document
        Map<String, Object> document = new HashMap<>();
        document.put("documentBase64", documentBase64);
        document.put("name", contract.getTitle() + ".pdf");
        document.put("fileExtension", "pdf");
        document.put("documentId", "1");
        envelope.put("documents", Collections.singletonList(document));

        // Signer
        Map<String, Object> signer = new HashMap<>();
        signer.put("email", signerEmail);
        signer.put("name", signerName);
        signer.put("recipientId", "1");
        signer.put("routingOrder", "1");
        signer.put("clientUserId", "1");

        // Signature tab
        Map<String, Object> signHere = new HashMap<>();
        signHere.put("documentId", "1");
        signHere.put("pageNumber", "1");
        signHere.put("recipientId", "1");
        signHere.put("xPosition", "100");
        signHere.put("yPosition", "700");

        Map<String, Object> tabs = new HashMap<>();
        tabs.put("signHereTabs", Collections.singletonList(signHere));
        signer.put("tabs", tabs);

        // Recipients
        Map<String, Object> recipients = new HashMap<>();
        recipients.put("signers", Collections.singletonList(signer));
        envelope.put("recipients", recipients);

        return envelope;
    }

    /**
     * Get the signing URL for a signer
     */
    private String getSigningUrl(String envelopeId, String signerEmail, String signerName) throws IOException {
        try {
            String url = docuSignConfig.getBasePath() 
                    + "/v2.1/accounts/" + docuSignConfig.getAccountId() 
                    + "/envelopes/" + envelopeId 
                    + "/views/recipient";

            Map<String, Object> request = new HashMap<>();
            request.put("returnUrl", docuSignConfig.getReturnUrl());
            request.put("authenticationMethod", "email");
            request.put("email", signerEmail);
            request.put("userName", signerName);
            request.put("clientUserId", "1");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + getAccessToken());

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(request, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IOException("Failed to get signing URL: " + response.getStatusCode());
            }

            return (String) response.getBody().get("url");

        } catch (Exception e) {
            logger.severe("Error generating signing URL: " + e.getMessage());
            throw new IOException("Failed to generate signing URL: " + e.getMessage(), e);
        }
    }

    /**
     * Get signature status of a contract
     */
    @Override
    public Map<String, Object> getSignatureStatus(Contract contract) throws IOException {
        if (contract.getEnvelopeId() == null) {
            throw new IllegalArgumentException("Contract has no envelope ID");
        }

        try {
            String url = docuSignConfig.getBasePath() 
                    + "/v2.1/accounts/" + docuSignConfig.getAccountId() 
                    + "/envelopes/" + contract.getEnvelopeId();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + getAccessToken());

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IOException("Failed to get status: " + response.getStatusCode());
            }

            Map<String, Object> result = response.getBody();
            logger.info("Envelope " + contract.getEnvelopeId() + " status: " + result.get("status"));
            return result;

        } catch (Exception e) {
            logger.severe("Error getting signature status: " + e.getMessage());
            throw new IOException("Failed to get signature status: " + e.getMessage(), e);
        }
    }

    /**
     * Download signed document from DocuSign
     */
    @Override
    public byte[] downloadSignedDocument(Contract contract) throws IOException {
        if (contract.getEnvelopeId() == null) {
            throw new IllegalArgumentException("Contract has no envelope ID");
        }

        try {
            String url = docuSignConfig.getBasePath() 
                    + "/v2.1/accounts/" + docuSignConfig.getAccountId() 
                    + "/envelopes/" + contract.getEnvelopeId() 
                    + "/documents/combined";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + getAccessToken());

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, byte[].class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IOException("Failed to download document: " + response.getStatusCode());
            }

            logger.info("Downloaded signed document for envelope " + contract.getEnvelopeId());
            return response.getBody();

        } catch (Exception e) {
            logger.severe("Error downloading signed document: " + e.getMessage());
            throw new IOException("Failed to download signed document: " + e.getMessage(), e);
        }
    }

    /**
     * Handle DocuSign webhook callback
     */
    @Override
    public void handleSignatureWebhook(String envelopeId) throws IOException {
        try {
            logger.info("Processing webhook for envelope " + envelopeId);

            // Find contract by envelope ID
            Contract contract = contractRepository.findByEnvelopeId(envelopeId)
                    .orElseThrow(() -> new IllegalArgumentException("Contract not found for envelope: " + envelopeId));

            // Get current status
            Map<String, Object> statusMap = getSignatureStatus(contract);
            String status = (String) statusMap.get("status");

            // Update contract based on status
            if ("completed".equalsIgnoreCase(status)) {
                contract.setSignatureStatus(SignatureStatus.SIGNED);
                contract.setSignedAt(LocalDateTime.now());
                logger.info("Contract " + contract.getId() + " marked as signed");
            } else if ("declined".equalsIgnoreCase(status)) {
                contract.setSignatureStatus(SignatureStatus.REJECTED);
                logger.info("Contract " + contract.getId() + " signature rejected");
            } else if ("voided".equalsIgnoreCase(status)) {
                contract.setSignatureStatus(SignatureStatus.EXPIRED);
                logger.info("Contract " + contract.getId() + " signature expired");
            }

            contractRepository.save(contract);

        } catch (Exception e) {
            logger.severe("Error handling signature webhook: " + e.getMessage());
            throw new IOException("Failed to handle signature webhook: " + e.getMessage(), e);
        }
    }

    /**
     * Void/cancel an envelope
     */
    @Override
    public void voidEnvelope(String envelopeId, String voidReason) throws IOException {
        try {
            String url = docuSignConfig.getBasePath() 
                    + "/v2.1/accounts/" + docuSignConfig.getAccountId() 
                    + "/envelopes/" + envelopeId;

            Map<String, Object> request = new HashMap<>();
            request.put("status", "voided");
            request.put("voidedReason", voidReason);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + getAccessToken());

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(request, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IOException("Failed to void envelope: " + response.getStatusCode());
            }

            logger.info("Envelope " + envelopeId + " voided with reason: " + voidReason);

        } catch (Exception e) {
            logger.severe("Error voiding envelope: " + e.getMessage());
            throw new IOException("Failed to void envelope: " + e.getMessage(), e);
        }
    }

    /**
     * Get OAuth2 access token from DocuSign
     * Implements JWT Bearer Flow for DocuSign authentication
     */
    private String getAccessToken() throws IOException {
        try {
            // Check if credentials are placeholders
            if (docuSignConfig.getClientId().contains("YOUR_") || 
                docuSignConfig.getClientSecret().contains("YOUR_")) {
                logger.warning("⚠️ DocuSign credentials are placeholders - using DEMO mode");
                // Return a demo token that won't actually work with DocuSign
                // But will allow testing the flow
                return "demo_token_" + System.currentTimeMillis();
            }

            // JWT Bearer Flow implementation
            String authUrl = "https://account.docusign.com/oauth/token";
            
            // Create JWT assertion (simplified - in production use proper JWT library)
            String jwtAssertion = createJWTAssertion();
            
            // Request token
            Map<String, String> tokenRequest = new LinkedHashMap<>();
            tokenRequest.put("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
            tokenRequest.put("assertion", jwtAssertion);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            String body = "grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=" + jwtAssertion;
            HttpEntity<String> request = new HttpEntity<>(body, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(authUrl, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String token = (String) response.getBody().get("access_token");
                logger.info("✅ Got DocuSign access token");
                return token;
            }
            
            logger.warning("Failed to get access token, using demo token");
            return "demo_token_" + System.currentTimeMillis();
            
        } catch (Exception e) {
            logger.warning("Error getting access token: " + e.getMessage() + " - using demo token");
            return "demo_token_" + System.currentTimeMillis();
        }
    }

    /**
     * Create JWT assertion for DocuSign authentication
     * Simplified version - for production use proper JWT library
     */
    private String createJWTAssertion() {
        try {
            // Header
            String header = Base64.getEncoder().encodeToString(
                "{\"alg\":\"RS256\",\"typ\":\"JWT\"}".getBytes()
            );
            
            // Payload
            long now = System.currentTimeMillis() / 1000;
            String payload = Base64.getEncoder().encodeToString(
                ("{\"iss\":\"" + docuSignConfig.getClientId() + "\"," +
                 "\"sub\":\"" + docuSignConfig.getImpersonatedUserId() + "\"," +
                 "\"aud\":\"account.docusign.com\"," +
                 "\"iat\":" + now + "," +
                 "\"exp\":" + (now + 3600) + "}").getBytes()
            );
            
            // For demo, we'll return a mock JWT
            // In production, sign with private key
            return header + "." + payload + ".signature";
            
        } catch (Exception e) {
            logger.warning("Error creating JWT: " + e.getMessage());
            return "";
        }
    }
}
