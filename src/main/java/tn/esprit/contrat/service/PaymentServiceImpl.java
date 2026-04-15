package tn.esprit.contrat.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.contrat.entity.*;
import tn.esprit.contrat.repository.ContractMilestoneRepository;
import tn.esprit.contrat.repository.ContractPaymentRepository;
import tn.esprit.contrat.repository.ContractHistoryRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final ContractMilestoneRepository milestoneRepo;
    private final ContractPaymentRepository   paymentRepo;
    private final ContractHistoryRepository   historyRepo;

    public PaymentServiceImpl(ContractMilestoneRepository milestoneRepo,
                              ContractPaymentRepository paymentRepo,
                              ContractHistoryRepository historyRepo) {
        this.milestoneRepo = milestoneRepo;
        this.paymentRepo   = paymentRepo;
        this.historyRepo   = historyRepo;
    }

    @Override
    @Transactional
    public InitPaymentResult initMilestonePayment(Long milestoneId, Long clientId) {
        ContractMilestone milestone = milestoneRepo.findById(milestoneId)
                .orElseThrow(() -> new IllegalArgumentException("Milestone " + milestoneId + " introuvable"));

        Contract contract = milestone.getContract();
        if (contract == null) {
            throw new IllegalStateException("Milestone sans contrat associé");
        }
        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new IllegalStateException("Le contrat doit être ACTIF pour payer une milestone. Statut : " + contract.getStatus());
        }

        // On autorise le paiement si la milestone n'est pas déjà payée
        if (milestone.getStatus() == MilestoneStatus.PAID) {
            throw new IllegalStateException("Cette milestone est déjà marquée comme payée.");
        }

        BigDecimal amount = milestone.getAmount() != null ? milestone.getAmount() : BigDecimal.ZERO;

        ContractPayment payment = new ContractPayment();
        payment.setContract(contract);
        payment.setMilestone(milestone);
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod("FAKE_PAYMEE");

        ContractPayment saved = paymentRepo.save(payment);

        // URL de "fake checkout" côté front (tu peux créer une page dédiée)
        String redirectUrl = "http://localhost:4200/front/payments/fake-checkout?paymentId=" + saved.getId();

        return new InitPaymentResult(saved, redirectUrl);
    }

    @Override
    @Transactional
    public ContractPayment simulateProviderCallback(Long paymentId, boolean success) {
        ContractPayment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Paiement " + paymentId + " introuvable"));

        ContractMilestone milestone = payment.getMilestone();
        Contract contract          = payment.getContract();

        if (success) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setPaidAt(LocalDateTime.now());

            if (milestone != null) {
                milestone.setStatus(MilestoneStatus.PAID);
                milestoneRepo.save(milestone);
            }

            ContractHistory h = new ContractHistory();
            h.setContract(contract);
            h.setAction(ContractAction.PAYMENT_COMPLETED);
            h.setPerformedBy(contract.getClientId());
            h.setOldValue("Milestone payée");
            h.setNewValue("Paiement complété pour la milestone #" + (milestone != null ? milestone.getId() : null)
                    + " montant " + payment.getAmount() + " " + contract.getCurrency());
            historyRepo.save(h);
        } else {
            payment.setStatus(PaymentStatus.FAILED);

            ContractHistory h = new ContractHistory();
            h.setContract(contract);
            h.setAction(ContractAction.PAYMENT_FAILED);
            h.setPerformedBy(contract.getClientId());
            h.setOldValue("Paiement en attente");
            h.setNewValue("Paiement échoué pour la milestone #" + (milestone != null ? milestone.getId() : null));
            historyRepo.save(h);
        }

        return paymentRepo.save(payment);
    }
}
