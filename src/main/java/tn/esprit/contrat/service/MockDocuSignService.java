package tn.esprit.contrat.service;

import org.springframework.stereotype.Service;
import tn.esprit.contrat.entity.Contract;
import tn.esprit.contrat.entity.SignatureStatus;
import tn.esprit.contrat.repository.ContractRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

/**
 * 🧪 Mock DocuSign Service for Testing
 * Simulates DocuSign behavior without needing real API credentials
 * Useful for testing the complete flow before integrating real DocuSign
 */
@Service
public class MockDocuSignService implements IDocuSignService {

    private static final Logger logger = Logger.getLogger(MockDocuSignService.class.getName());
    private final ContractRepository contractRepository;

    // Simulated state storage
    private static final Map<String, MockEnvelope> simulatedEnvelopes = new HashMap<>();

    public MockDocuSignService(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    @Override
    public String sendForSignature(Contract contract, String signerEmail, String signerName) throws IOException {
        logger.info("🧪 MOCK: Sending contract " + contract.getId() + " for signature to " + signerEmail);

        try {
            // Create a mock envelope
            String envelopeId = "MOCK_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String signingUrl = "http://localhost:4200/mock-signing?envelope=" + envelopeId + "&email=" + signerEmail;

            // Store in memory
            MockEnvelope envelope = new MockEnvelope();
            envelope.envelopeId = envelopeId;
            envelope.signerEmail = signerEmail;
            envelope.signerName = signerName;
            envelope.status = "sent";
            envelope.createdAt = System.currentTimeMillis();
            simulatedEnvelopes.put(envelopeId, envelope);

            // Update contract
            contract.setEnvelopeId(envelopeId);
            contract.setSigningUrl(signingUrl);
            contract.setSignatureStatus(SignatureStatus.PENDING);
            contractRepository.save(contract);

            logger.info("✅ MOCK: Envelope created: " + envelopeId);
            logger.info("📧 MOCK: Email would be sent to: " + signerEmail);
            logger.info("🔗 MOCK: Signing URL: " + signingUrl);

            return signingUrl;

        } catch (Exception e) {
            logger.severe("MOCK Error: " + e.getMessage());
            throw new IOException("Mock DocuSign error: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getSignatureStatus(Contract contract) throws IOException {
        logger.info("🧪 MOCK: Checking status for envelope " + contract.getEnvelopeId());

        if (contract.getEnvelopeId() == null) {
            throw new IllegalArgumentException("Contract has no envelope ID");
        }

        try {
            MockEnvelope envelope = simulatedEnvelopes.get(contract.getEnvelopeId());

            if (envelope == null) {
                // Return pending if not found (user might have just signed)
                Map<String, Object> result = new HashMap<>();
                result.put("status", contract.getSignatureStatus() != null ? contract.getSignatureStatus().toString() : "PENDING");
                result.put("docuSignStatus", "sent");
                return result;
            }

            // Simulate automatic signing after 30 seconds of polling
            long timeSinceCreation = System.currentTimeMillis() - envelope.createdAt;
            if (timeSinceCreation > 30000) { // 30 seconds = auto-sign for demo
                envelope.status = "completed";
                contract.setSignatureStatus(SignatureStatus.SIGNED);
                contract.setSignedAt(LocalDateTime.now());
                contract.setSignedDocumentUrl("mock-pdf-" + contract.getId() + ".pdf");
                contractRepository.save(contract);
                logger.info("✅ MOCK: Contract auto-signed after 30 seconds");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("status", envelope.status);
            result.put("docuSignStatus", envelope.status);
            result.put("signatureStatus", envelope.status.equals("completed") ? "SIGNED" : "PENDING");
            result.put("signerName", envelope.signerName);
            result.put("signerEmail", envelope.signerEmail);

            return result;

        } catch (Exception e) {
            logger.severe("MOCK Error: " + e.getMessage());
            throw new IOException("Mock DocuSign error: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] downloadSignedDocument(Contract contract) throws IOException {
        logger.info("🧪 MOCK: Downloading signed document for " + contract.getId());

        try {
            // Generate a mock PDF
            String pdfContent = "Mock PDF Document\n\n" +
                    "Contract: " + contract.getTitle() + "\n" +
                    "Signed by: " + contract.getEnvelopeId() + "\n" +
                    "Date: " + LocalDateTime.now() + "\n" +
                    "\n[MOCK SIGNATURE PDF - FOR TESTING ONLY]";

            logger.info("✅ MOCK: Generated mock PDF");
            return pdfContent.getBytes();

        } catch (Exception e) {
            logger.severe("MOCK Error: " + e.getMessage());
            throw new IOException("Mock DocuSign error: " + e.getMessage(), e);
        }
    }

    @Override
    public void handleSignatureWebhook(String envelopeId) throws IOException {
        logger.info("🧪 MOCK: Processing webhook for envelope " + envelopeId);

        try {
            MockEnvelope envelope = simulatedEnvelopes.get(envelopeId);
            if (envelope != null) {
                envelope.status = "completed";
                logger.info("✅ MOCK: Envelope marked as completed");
            }

        } catch (Exception e) {
            logger.severe("MOCK Error: " + e.getMessage());
            throw new IOException("Mock DocuSign error: " + e.getMessage(), e);
        }
    }

    @Override
    public void voidEnvelope(String envelopeId, String voidReason) throws IOException {
        logger.info("🧪 MOCK: Voiding envelope " + envelopeId);

        try {
            MockEnvelope envelope = simulatedEnvelopes.get(envelopeId);
            if (envelope != null) {
                envelope.status = "voided";
                simulatedEnvelopes.remove(envelopeId);
                logger.info("✅ MOCK: Envelope voided");
            }

        } catch (Exception e) {
            logger.severe("MOCK Error: " + e.getMessage());
            throw new IOException("Mock DocuSign error: " + e.getMessage(), e);
        }
    }

    /**
     * Inner class to simulate DocuSign envelope
     */
    private static class MockEnvelope {
        String envelopeId;
        String signerEmail;
        String signerName;
        String status;
        long createdAt;
    }
}
