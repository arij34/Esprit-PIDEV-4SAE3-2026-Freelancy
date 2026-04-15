package tn.esprit.contrat.service;

import tn.esprit.contrat.entity.ContractPayment;

public interface PaymentService {

    /**
     * Initialise un paiement fictif pour une milestone donnée.
     * Ne contacte aucun prestataire externe: génère simplement un
     * enregistrement ContractPayment et une URL de "fake checkout".
     */
    InitPaymentResult initMilestonePayment(Long milestoneId, Long clientId);

    /**
     * Simule le retour prestataire (succès ou échec) et met à jour
     * le paiement + la milestone + l'historique du contrat.
     */
    ContractPayment simulateProviderCallback(Long paymentId, boolean success);

    class InitPaymentResult {
        public final ContractPayment payment;
        public final String redirectUrl;

        public InitPaymentResult(ContractPayment payment, String redirectUrl) {
            this.payment = payment;
            this.redirectUrl = redirectUrl;
        }
    }
}
