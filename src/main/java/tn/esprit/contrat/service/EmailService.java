package tn.esprit.contrat.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.esprit.contrat.entity.Contract;

import java.io.File;
import java.util.logging.Logger;

@Service
public class EmailService {

    private static final Logger logger = Logger.getLogger(EmailService.class.getName());

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired(required = false)
    private RestTemplate restTemplate;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${user-service.url:http://localhost:8090}")
    private String userServiceUrl;

    // ═══════════════════════════════════════════════════════════════
    // MÉTHODES PUBLIQUES — appelées depuis ContractServiceImpl
    // ═══════════════════════════════════════════════════════════════

    /**
     * Envoie une notification au freelancer pour signer le contrat.
     * Récupère l'email du freelancer via l'user-service.
     */
    public void sendSignatureRequestToFreelancer(Contract contract, String authHeader) {
        if (mailSender == null) {
            logger.warning("⚠️ Mail sender non configuré – email non envoyé");
            return;
        }

        UserInfo freelancer = getUserInfo(contract.getFreelancerId(), authHeader);
        if (freelancer == null || freelancer.email == null) {
            logger.warning("⚠️ Email freelancer introuvable pour userId=" + contract.getFreelancerId());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(freelancer.email);
            helper.setSubject("📝 Contrat en attente de votre signature – " + contract.getTitle());

            String signingLink = frontendUrl + "/front-office/contracts-freelancer/" + contract.getId();

            String html = buildEmailTemplate(
                    "Signature requise",
                    "Bonjour " + freelancer.name + ",",
                    "Le client a soumis le contrat <strong>\"" + contract.getTitle() + "\"</strong> "
                            + "d'un montant de <strong>" + contract.getTotalAmount() + " " + contract.getCurrency() + "</strong>. "
                            + "Veuillez le signer pour activer votre collaboration.",
                    signingLink,
                    "Signer le contrat",
                    "Ce lien de signature est valable 7 jours."
            );
            helper.setText(html, true);
            mailSender.send(message);
            logger.info("✅ Email de signature envoyé au freelancer : " + freelancer.email);
        } catch (MessagingException e) {
            logger.severe("❌ Erreur envoi email freelancer : " + e.getMessage());
        }
    }

    /**
     * Envoie une notification au client pour signer le contrat.
     * Récupère l'email du client via l'user-service.
     */
    public void sendSignatureRequestToClient(Contract contract, String authHeader) {
        if (mailSender == null) {
            logger.warning("⚠️ Mail sender non configuré – email non envoyé");
            return;
        }

        UserInfo client = getUserInfo(contract.getClientId(), authHeader);
        if (client == null || client.email == null) {
            logger.warning("⚠️ Email client introuvable pour userId=" + contract.getClientId());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(client.email);
            helper.setSubject("✍️ Le freelancer a signé – À votre tour ! – " + contract.getTitle());

            String signingLink = frontendUrl + "/front/contracts?projectId=" + contract.getProjectId()
                    + "&signContractId=" + contract.getId();

            String html = buildEmailTemplate(
                    "Votre signature est requise",
                    "Bonjour " + client.name + ",",
                    "Le freelancer a signé le contrat <strong>\"" + contract.getTitle() + "\"</strong> "
                            + "d'un montant de <strong>" + contract.getTotalAmount() + " " + contract.getCurrency() + "</strong>. "
                            + "Il ne reste plus que votre signature pour activer ce contrat.",
                    signingLink,
                    "✍️ Signer le contrat maintenant",
                    "Ce lien vous amènera directement à la page de signature. Valable 7 jours."
            );
            helper.setText(html, true);
            mailSender.send(message);
            logger.info("✅ Email de signature envoyé au client : " + client.email);
        } catch (MessagingException e) {
            logger.severe("❌ Erreur envoi email client : " + e.getMessage());
        }
    }

    /**
     * Envoie le contrat signé (PDF en pièce jointe) aux deux parties.
     * Récupère les emails depuis l'user-service.
     */
    public void sendSignedContractToParties(Contract contract, String authHeader) {
        if (mailSender == null) {
            logger.warning("⚠️ Mail sender non configuré – emails de contrat signé non envoyés");
            return;
        }

        UserInfo client     = getUserInfo(contract.getClientId(), authHeader);
        UserInfo freelancer = getUserInfo(contract.getFreelancerId(), authHeader);

        String pdfPath = contract.getPdfUrl();

        if (client != null && client.email != null) {
            sendSignedContractEmail(contract, client.email, client.name, pdfPath, "client");
        } else {
            logger.warning("⚠️ Email client introuvable — contrat signé non envoyé au client");
        }

        if (freelancer != null && freelancer.email != null) {
            sendSignedContractEmail(contract, freelancer.email, freelancer.name, pdfPath, "freelancer");
        } else {
            logger.warning("⚠️ Email freelancer introuvable — contrat signé non envoyé au freelancer");
        }
    }

    /**
     * Notifie le client qu'il y a des modifications proposées par le freelancer.
     */
    public void sendModificationProposalToClient(Contract contract, String modifications, String authHeader) {
        if (mailSender == null) {
            logger.warning("⚠️ Mail sender non configuré – email non envoyé");
            return;
        }

        UserInfo client = getUserInfo(contract.getClientId(), authHeader);
        if (client == null || client.email == null) {
            logger.warning("⚠️ Email client introuvable pour userId=" + contract.getClientId());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(client.email);
            helper.setSubject("🔄 Propositions de modifications – " + contract.getTitle());

            String reviewLink = frontendUrl + "/front/contracts?projectId=" + contract.getProjectId();

            String html = buildEmailTemplate(
                    "Propositions de modifications",
                    "Bonjour " + client.name + ",",
                    "Le freelancer a proposé des modifications au contrat <strong>\"" + contract.getTitle() + "\"</strong>.<br><br>"
                            + "<strong>Modifications proposées :</strong><br>"
                            + "<div style='background:#f8fafc;padding:12px;border-radius:6px;margin:8px 0;'>" + modifications + "</div>"
                            + "Veuillez accepter ou refuser ces modifications.",
                    reviewLink,
                    "Voir les modifications",
                    "Si vous refusez, le contrat passera en statut DISPUTE."
            );
            helper.setText(html, true);
            mailSender.send(message);
            logger.info("✅ Email modifications envoyé au client : " + client.email);
        } catch (MessagingException e) {
            logger.severe("❌ Erreur envoi email modifications : " + e.getMessage());
        }
    }

    /**
     * Notifie une partie qu'il y a un litige (DISPUTED).
     */
    public void sendDisputeNotification(Contract contract, String reason, String authHeader) {
        if (mailSender == null) return;

        UserInfo freelancer = getUserInfo(contract.getFreelancerId(), authHeader);
        UserInfo client     = getUserInfo(contract.getClientId(), authHeader);

        if (freelancer != null && freelancer.email != null) {
            sendDisputeEmail(contract, freelancer.email, freelancer.name, reason);
        }
        if (client != null && client.email != null) {
            sendDisputeEmail(contract, client.email, client.name, reason);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // MÉTHODES PRIVÉES
    // ═══════════════════════════════════════════════════════════════

    private void sendSignedContractEmail(Contract contract, String recipientEmail,
                                         String recipientName, String pdfPath, String role) {
        try {
            boolean hasPdf = pdfPath != null && !pdfPath.isBlank() && new File(pdfPath).exists();
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(recipientEmail);
            helper.setSubject("🎉 Contrat signé et activé – " + contract.getTitle());

            String html = buildEmailTemplate(
                    "Contrat activé !",
                    "Bonjour " + recipientName + ",",
                    "Excellente nouvelle ! Le contrat <strong>\"" + contract.getTitle() + "\"</strong> "
                            + "a été signé par les deux parties et est maintenant <strong>ACTIF</strong>. "
                            + "Montant total : <strong>" + contract.getTotalAmount() + " " + contract.getCurrency() + "</strong>. "
                            + (hasPdf ? "Vous trouverez le contrat signé en pièce jointe." : "Le PDF sera disponible prochainement."),
                    frontendUrl + "/front/contracts",
                    "Voir le contrat",
                    "Ce contrat a été signé le " + (contract.getClientSignedAt() != null
                            ? contract.getClientSignedAt().toString() : "aujourd'hui") + "."
            );
            helper.setText(html, true);

            // Joindre le PDF si disponible
            if (hasPdf) {
                File pdfFile = new File(pdfPath);
                FileSystemResource resource = new FileSystemResource(pdfFile);
                helper.addAttachment("contrat-" + contract.getId() + ".pdf", resource);
                logger.info("📎 PDF joint à l'email : " + pdfPath);
            } else {
                logger.warning("⚠️ PDF non disponible (" + pdfPath + ") — email envoyé sans pièce jointe");
            }

            mailSender.send(message);
            logger.info("✅ Email contrat signé envoyé à " + role + " : " + recipientEmail);
        } catch (MessagingException e) {
            logger.severe("❌ Erreur envoi email " + role + " : " + e.getMessage());
        }
    }

    private void sendDisputeEmail(Contract contract, String recipientEmail,
                                  String recipientName, String reason) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(recipientEmail);
            helper.setSubject("⚠️ Litige ouvert – " + contract.getTitle());

            String html = buildEmailTemplate(
                    "Contrat en litige",
                    "Bonjour " + recipientName + ",",
                    "Le contrat <strong>\"" + contract.getTitle() + "\"</strong> est passé en statut <strong>DISPUTE</strong>.<br><br>"
                            + "<strong>Raison :</strong> " + reason,
                    frontendUrl + "/front/contracts",
                    "Voir le contrat",
                    "Un administrateur va examiner ce litige prochainement."
            );
            helper.setText(html, true);
            mailSender.send(message);
            logger.info("✅ Email de litige envoyé : " + recipientEmail);
        } catch (MessagingException e) {
            logger.severe("❌ Erreur envoi email litige : " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // RÉCUPÉRATION DES INFOS UTILISATEUR
    // ═══════════════════════════════════════════════════════════════

    /**
     * Récupère l'email et le nom d'un utilisateur depuis l'user-service.
     * Retourne null si indisponible.
     */
    private UserInfo getUserInfo(Long userId, String authHeader) {
        if (userId == null || userId == 0L) return null;
        if (restTemplate == null) {
            logger.warning("⚠️ RestTemplate non configuré — impossible de récupérer l'email de l'utilisateur");
            return null;
        }

        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            if (authHeader != null && !authHeader.isBlank()) {
                headers.set("Authorization", authHeader);
            }
            org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);

            org.springframework.http.ResponseEntity<java.util.Map> response = restTemplate.exchange(
                    userServiceUrl + "/users/" + userId,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    java.util.Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                java.util.Map<?, ?> body = response.getBody();
                String email     = getStr(body, "email");
                String firstName = getStr(body, "firstName");
                String lastName  = getStr(body, "lastName");
                String name      = (firstName + " " + lastName).trim();
                if (name.isBlank()) name = getStr(body, "username");

                logger.info("✅ UserInfo récupéré pour userId=" + userId + " : email=" + email);
                return new UserInfo(email, name.isBlank() ? "Utilisateur" : name);
            }
        } catch (Exception e) {
            logger.warning("⚠️ Impossible de récupérer UserInfo pour userId=" + userId + " : " + e.getMessage());
        }
        return null;
    }

    private String getStr(java.util.Map<?, ?> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : "";
    }

    // ═══════════════════════════════════════════════════════════════
    // CLASSE INTERNE — UserInfo
    // ═══════════════════════════════════════════════════════════════

    public static class UserInfo {
        public final String email;
        public final String name;

        public UserInfo(String email, String name) {
            this.email = email;
            this.name  = name;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // TEMPLATE HTML
    // ═══════════════════════════════════════════════════════════════

    private String buildEmailTemplate(String title, String greeting, String body,
                                      String ctaUrl, String ctaText, String footer) {
        return "<!DOCTYPE html>"
                + "<html><head><meta charset='UTF-8'></head>"
                + "<body style='margin:0;padding:0;background:#f1f5f9;font-family:Arial,sans-serif;'>"
                + "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f1f5f9;padding:30px 0;'>"
                + "<tr><td align='center'>"
                + "<table width='600' cellpadding='0' cellspacing='0' style='background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 2px 10px rgba(0,0,0,0.08);'>"
                + "<tr><td style='background:linear-gradient(135deg,#2563eb,#1d4ed8);padding:30px 40px;text-align:center;'>"
                + "<h1 style='color:#ffffff;margin:0;font-size:22px;'>Freelancy</h1>"
                + "<p style='color:#bfdbfe;margin:8px 0 0;font-size:14px;'>" + title + "</p>"
                + "</td></tr>"
                + "<tr><td style='padding:35px 40px;'>"
                + "<p style='font-size:16px;color:#1e293b;margin:0 0 15px;'>" + greeting + "</p>"
                + "<p style='font-size:15px;color:#475569;line-height:1.6;margin:0 0 25px;'>" + body + "</p>"
                + "<div style='text-align:center;margin:30px 0;'>"
                + "<a href='" + ctaUrl + "' style='background:#2563eb;color:#ffffff;padding:14px 32px;border-radius:8px;"
                + "text-decoration:none;font-size:15px;font-weight:bold;display:inline-block;'>" + ctaText + "</a>"
                + "</div>"
                + "<hr style='border:none;border-top:1px solid #e2e8f0;margin:25px 0;'/>"
                + "<p style='font-size:13px;color:#94a3b8;margin:0;'>" + footer + "</p>"
                + "</td></tr>"
                + "<tr><td style='background:#f8fafc;padding:15px 40px;text-align:center;'>"
                + "<p style='font-size:12px;color:#94a3b8;margin:0;'>© 2026 Freelancy – Plateforme de freelance ESPRIT</p>"
                + "</td></tr>"
                + "</table>"
                + "</td></tr></table></body></html>";
    }
}