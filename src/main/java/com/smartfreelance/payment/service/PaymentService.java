package com.smartfreelance.payment.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartfreelance.payment.controller.dto.PaySubscriptionRequest;
import com.smartfreelance.payment.controller.dto.PaySubscriptionResponse;
import com.smartfreelance.payment.model.Payment;
import com.smartfreelance.payment.model.PaymentStatus;
import com.smartfreelance.payment.repository.PaymentRepository;
import com.smartfreelance.payment.service.dto.SubscriptionDto;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SubscriptionClient subscriptionClient;
    private final EmailService emailService;

    public PaymentService(PaymentRepository paymentRepository,
                          SubscriptionClient subscriptionClient,
                          EmailService emailService) {
        this.paymentRepository = paymentRepository;
        this.subscriptionClient = subscriptionClient;
        this.emailService = emailService;
    }

    @Transactional
    public PaySubscriptionResponse paySubscription(PaySubscriptionRequest req, String userEmail) {
        // simple simulation rule: card number ending with 0 => FAILED, otherwise SUCCESS
        PaymentStatus status = req.cardNumber().endsWith("0") ? PaymentStatus.FAILED : PaymentStatus.SUCCESS;

        BigDecimal originalAmount = new BigDecimal("29.00");

        // Discount rule: every 6th SUCCESS payment gets -50%
        // i.e. after 5 SUCCESS, the next one (6th) is discounted.
        long previousSuccessCount = paymentRepository.countByUserIdAndStatus(req.userId(), PaymentStatus.SUCCESS);
        boolean eligibleForDiscount = (previousSuccessCount > 0) && (previousSuccessCount % 5 == 0);

        // Apply discount only if THIS payment is SUCCESS.
        boolean applyDiscount = (status == PaymentStatus.SUCCESS) && eligibleForDiscount;
        int discountPercent = applyDiscount ? 50 : 0;
        BigDecimal finalAmount = applyDiscount
                ? originalAmount.multiply(new BigDecimal("0.50"))
                : originalAmount;

        Payment p = new Payment();
        p.setUserId(req.userId());
        p.setPlan(req.plan().toUpperCase());
        // For failed payments we keep discount fields null/0 (to avoid confusion in history)
        p.setOriginalAmount(status == PaymentStatus.SUCCESS ? originalAmount : null);
        p.setDiscountPercent(status == PaymentStatus.SUCCESS ? discountPercent : 0);
        p.setAmount(finalAmount);
        p.setStatus(status);
        p.setCardLast4(req.cardNumber().substring(12));
        Payment saved = paymentRepository.save(p);

        if (status == PaymentStatus.SUCCESS) {
            // activate VIP for 30 days
            SubscriptionDto sub = subscriptionClient.activateVip(req.userId(), 30);

            // Send email receipt (best effort)
            try {
                if (sub != null) {
                    emailService.sendVipUpgradeEmail(
                            userEmail,
                            originalAmount,
                            discountPercent,
                            saved.getAmount(),
                            sub.endDate()
                    );
                } else {
                    // still send without end date if subscription service didn't return anything
                    emailService.sendVipUpgradeEmail(
                            userEmail,
                            originalAmount,
                            discountPercent,
                            saved.getAmount(),
                            null
                    );
                }
            } catch (Exception e) {
                // Do not fail the payment if email cannot be sent.
                // The payment and subscription activation are already successful.
                System.err.println("Failed to send payment email: " + e.getMessage());
            }

            return new PaySubscriptionResponse(
                    saved.getId(),
                    status,
                    applyDiscount
                            ? "Payment SUCCESS. Réduction 50% appliquée. VIP activé pour 30 jours."
                            : "Payment SUCCESS. VIP activé pour 30 jours.",
                    sub != null ? sub.endDate() : null,
                    saved.getDiscountPercent(),
                    saved.getOriginalAmount(),
                    saved.getAmount()
            );
        }

        return new PaySubscriptionResponse(
                saved.getId(),
                status,
                "Payment FAILED.",
                null,
                saved.getDiscountPercent(),
                saved.getOriginalAmount(),
                saved.getAmount()
        );
    }
}
