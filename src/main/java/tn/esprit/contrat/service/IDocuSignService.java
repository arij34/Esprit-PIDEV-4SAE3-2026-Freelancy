package tn.esprit.contrat.service;

import tn.esprit.contrat.entity.Contract;

import java.io.IOException;
import java.util.Map;

/**
 * DocuSign eSignature Service Interface
 * Defines operations for digital signature workflows
 */
public interface IDocuSignService {

    /**
     * Send contract for signature via DocuSign
     * @param contract Contract to send for signature
     * @param signerEmail Email of the signer
     * @param signerName Name of the signer
     * @return Signing URL for the signer
     */
    String sendForSignature(Contract contract, String signerEmail, String signerName) throws IOException;

    /**
     * Get signature status of a contract
     * @param contract Contract to check status
     * @return Map with status information
     */
    Map<String, Object> getSignatureStatus(Contract contract) throws IOException;

    /**
     * Download signed document from DocuSign
     * @param contract Contract with signed document
     * @return Byte array of the signed PDF
     */
    byte[] downloadSignedDocument(Contract contract) throws IOException;

    /**
     * Handle DocuSign webhook callback when envelope status changes
     * @param envelopeId DocuSign envelope ID
     */
    void handleSignatureWebhook(String envelopeId) throws IOException;

    /**
     * Void/cancel an envelope
     * @param envelopeId DocuSign envelope ID
     * @param voidReason Reason for voiding
     */
    void voidEnvelope(String envelopeId, String voidReason) throws IOException;
}
