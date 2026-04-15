package tn.esprit.contrat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.contrat.entity.ContractPayment;
import tn.esprit.contrat.service.PaymentService;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Initialise un paiement fictif pour une milestone.
     * Retourne l'ID du paiement et une URL de "fake checkout" côté front.
     */
    @PostMapping("/milestones/{milestoneId}/init")
    public ResponseEntity<?> initMilestonePayment(@PathVariable Long milestoneId,
                                                  @RequestParam Long clientId) {
        try {
            PaymentService.InitPaymentResult result = paymentService.initMilestonePayment(milestoneId, clientId);
            ContractPayment p = result.payment;
            return ResponseEntity.ok(Map.of(
                    "paymentId", p.getId(),
                    "amount", p.getAmount(),
                    "status", p.getStatus().name(),
                    "redirectUrl", result.redirectUrl
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Endpoint de simulation pour un prestataire type Paymee.
     * Le front académique peut appeler cette route pour simuler un
     * paiement réussi ou échoué sans carte bancaire réelle.
     */
    @PostMapping("/{paymentId}/simulate")
    public ResponseEntity<?> simulatePayment(@PathVariable Long paymentId,
                                             @RequestParam(defaultValue = "true") boolean success) {
        try {
            ContractPayment updated = paymentService.simulateProviderCallback(paymentId, success);
            return ResponseEntity.ok(Map.of(
                    "paymentId", updated.getId(),
                    "status", updated.getStatus().name(),
                    "paidAt", updated.getPaidAt()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }
}
