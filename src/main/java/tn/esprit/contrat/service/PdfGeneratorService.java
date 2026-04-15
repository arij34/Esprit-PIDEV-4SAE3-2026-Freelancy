package tn.esprit.contrat.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;
import tn.esprit.contrat.entity.Contract;
import tn.esprit.contrat.entity.ContractMilestone;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGeneratorService {

        // Dossier où les PDF sont générés (relatif au répertoire de travail de l'application).
        // On utilise "pdfs" (sans ./) pour que l'URL exposée soit "pdfs/contract-XX.pdf".
        private static final String PDF_DIR = "pdfs";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Couleurs de la charte graphique
    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(37, 99, 235);    // bleu
    private static final DeviceRgb ACCENT_COLOR  = new DeviceRgb(16, 185, 129);   // vert
    private static final DeviceRgb LIGHT_GRAY    = new DeviceRgb(248, 250, 252);
    private static final DeviceRgb BORDER_GRAY   = new DeviceRgb(226, 232, 240);

        /**
         * Génère le PDF du contrat, le sauvegarde et retourne le chemin.
         *
         * @param contract       le contrat à générer (doit être ACTIVE, les deux signatures renseignées)
         * @param clientName     nom complet du client (optionnel, peut être null)
         * @param freelancerName nom complet du freelancer (optionnel, peut être null)
         * @param projectTitle   titre du projet (optionnel, peut être null)
         * @return chemin relatif du fichier PDF généré
         */
        public String generateContractPdf(Contract contract,
                                                                          String clientName,
                                                                          String freelancerName,
                                                                          String projectTitle) throws IOException {

        // ── Créer le dossier si besoin ─────────────────────────────────────────
        var dirPath  = Paths.get(PDF_DIR);
        Files.createDirectories(dirPath);

        String fileName = "contract-" + contract.getId() + ".pdf";
        var filePath = dirPath.resolve(fileName);

        PdfFont fontRegular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont fontBold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        try (PdfDocument pdf = new PdfDocument(new PdfWriter(filePath.toString()));
             Document doc = new Document(pdf, PageSize.A4)) {

            doc.setMargins(40, 50, 40, 50);

            // ── En-tête ───────────────────────────────────────────────────────
            addHeader(doc, fontBold, fontRegular, contract);

            // ── Séparateur ────────────────────────────────────────────────────
            addDivider(doc);

            // ── Informations générales ────────────────────────────────────────
            addSectionTitle(doc, fontBold, "Informations Générales");
            addInfoTable(doc, fontBold, fontRegular, contract, clientName, freelancerName, projectTitle);

            doc.add(new Paragraph("\n"));

            // ── Description & Clauses ─────────────────────────────────────────
            addSectionTitle(doc, fontBold, "Description et Clauses");
            addDescription(doc, fontRegular, contract.getDescription());

            doc.add(new Paragraph("\n"));

            // ── Milestones ────────────────────────────────────────────────────
            if (contract.getMilestones() != null && !contract.getMilestones().isEmpty()) {
                addSectionTitle(doc, fontBold, "Milestones");
                addMilestonesTable(doc, fontBold, fontRegular, contract);
            }

            doc.add(new Paragraph("\n"));

            // ── Signatures ────────────────────────────────────────────────────
            addSectionTitle(doc, fontBold, "Signatures");
            addSignaturesTable(doc, fontBold, fontRegular, contract, clientName, freelancerName);

            // ── Pied de page ──────────────────────────────────────────────────
            addFooter(doc, fontRegular, contract);
        }

                System.out.println("✅ PDF généré : " + filePath.toAbsolutePath());

                // Chemin relatif utilisé côté frontend / API (exposé via ResourceHandler /pdfs/**)
                return PDF_DIR + "/" + fileName;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sections privées
    // ─────────────────────────────────────────────────────────────────────────

    private void addHeader(Document doc, PdfFont fontBold, PdfFont fontRegular, Contract contract) throws IOException {

        // Bandeau coloré titre
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                .setWidth(UnitValue.createPercentValue(100));

        Cell titleCell = new Cell()
                .add(new Paragraph("CONTRAT DE PRESTATION")
                        .setFont(fontBold).setFontSize(18).setFontColor(ColorConstants.WHITE))
                .add(new Paragraph(contract.getTitle())
                        .setFont(fontRegular).setFontSize(11).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(PRIMARY_COLOR)
                .setPadding(15)
                .setBorder(null);

        Cell statusCell = new Cell()
                .add(new Paragraph("ACTIF").setFont(fontBold).setFontSize(14)
                        .setFontColor(ColorConstants.WHITE).setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph("Ref #" + contract.getId()).setFont(fontRegular).setFontSize(9)
                        .setFontColor(ColorConstants.WHITE).setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(ACCENT_COLOR)
                .setPadding(15)
                .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE)
                .setBorder(null);

        headerTable.addCell(titleCell);
        headerTable.addCell(statusCell);
        doc.add(headerTable);
    }

    private void addDivider(Document doc) {
        doc.add(new Paragraph()
                .setBorderBottom(new SolidBorder(BORDER_GRAY, 1))
                .setMarginTop(10).setMarginBottom(10));
    }

    private void addSectionTitle(Document doc, PdfFont fontBold, String title) {
        doc.add(new Paragraph(title)
                .setFont(fontBold).setFontSize(12)
                .setFontColor(PRIMARY_COLOR)
                .setBorderBottom(new SolidBorder(PRIMARY_COLOR, 1.5f))
                .setPaddingBottom(4).setMarginBottom(8));
    }

        private void addInfoTable(Document doc,
                                                          PdfFont fontBold,
                                                          PdfFont fontRegular,
                                                          Contract contract,
                                                          String clientName,
                                                          String freelancerName,
                                                          String projectTitle) {

        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .setWidth(UnitValue.createPercentValue(100));

        String clientLabel = clientName != null && !clientName.isBlank()
                ? clientName
                : "Client #" + contract.getClientId();
        String freelancerLabel = freelancerName != null && !freelancerName.isBlank()
                ? freelancerName
                : "Freelancer #" + contract.getFreelancerId();
        String projectLabel = projectTitle != null && !projectTitle.isBlank()
                ? projectTitle
                : "Project #" + contract.getProjectId();

        String[][] rows = {
                {"Client",           clientLabel},
                {"Freelancer",       freelancerLabel},
                {"Montant total",    formatAmount(contract.getTotalAmount(), contract.getCurrency())},
                {"Date de début",    contract.getStartDate() != null ? contract.getStartDate().format(DATE_FMT) : "-"},
                {"Date de fin",      contract.getEndDate()   != null ? contract.getEndDate().format(DATE_FMT)   : "-"},
                {"Projet",           projectLabel},
        };

        boolean alternate = false;
        for (String[] row : rows) {
            Cell keyCell = new Cell().add(new Paragraph(row[0]).setFont(fontBold).setFontSize(9))
                    .setBackgroundColor(alternate ? LIGHT_GRAY : ColorConstants.WHITE)
                    .setPadding(6).setBorder(new SolidBorder(BORDER_GRAY, 0.5f));
            Cell valCell = new Cell().add(new Paragraph(row[1]).setFont(fontRegular).setFontSize(9))
                    .setBackgroundColor(alternate ? LIGHT_GRAY : ColorConstants.WHITE)
                    .setPadding(6).setBorder(new SolidBorder(BORDER_GRAY, 0.5f));
            table.addCell(keyCell);
            table.addCell(valCell);
            alternate = !alternate;
        }
        doc.add(table);
    }

    private void addDescription(Document doc, PdfFont fontRegular, String description) {
        if (description == null || description.isBlank()) return;

        // Séparer clauses et description générale
        String[] parts = description.split("=== CLAUSES ===");

        if (parts.length > 0 && !parts[0].isBlank()) {
            doc.add(new Paragraph(parts[0].trim())
                    .setFont(fontRegular).setFontSize(9)
                    .setBackgroundColor(LIGHT_GRAY)
                    .setPadding(10).setMarginBottom(8));
        }

        if (parts.length > 1) {
            String[] clauses = parts[1].trim().split("\n\n");
            for (String clause : clauses) {
                if (clause.isBlank()) continue;
                doc.add(new Paragraph("▸  " + clause.trim())
                        .setFont(fontRegular).setFontSize(9)
                        .setBorderLeft(new SolidBorder(PRIMARY_COLOR, 3))
                        .setPaddingLeft(8).setMarginBottom(6));
            }
        }
    }

    private void addMilestonesTable(Document doc, PdfFont fontBold, PdfFont fontRegular, Contract contract) {

        Table table = new Table(UnitValue.createPercentArray(new float[]{5, 35, 30, 15, 15}))
                .setWidth(UnitValue.createPercentValue(100));

        // En-tête
        String[] headers = {"#", "Titre", "Description", "Montant", "Deadline"};
        for (String h : headers) {
            table.addHeaderCell(new Cell()
                    .add(new Paragraph(h).setFont(fontBold).setFontSize(9).setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(PRIMARY_COLOR).setPadding(6).setBorder(null));
        }

        BigDecimal total = BigDecimal.ZERO;
        boolean alternate = false;

        for (ContractMilestone m : contract.getMilestones()) {
            DeviceRgb bg = alternate ? LIGHT_GRAY : new DeviceRgb(255, 255, 255);

            table.addCell(cellOf(String.valueOf(m.getOrderIndex()), fontRegular, 9, bg));
            table.addCell(cellOf(m.getTitle(), fontBold, 9, bg));
            table.addCell(cellOf(m.getDescription() != null ? m.getDescription() : "", fontRegular, 8, bg));
            table.addCell(cellOf(formatAmount(m.getAmount(), contract.getCurrency()), fontRegular, 9, bg));
            table.addCell(cellOf(m.getDeadline() != null ? m.getDeadline().format(DATE_FMT) : "-", fontRegular, 9, bg));

            if (m.getAmount() != null) total = total.add(m.getAmount());
            alternate = !alternate;
        }

        // Ligne total
        table.addCell(new Cell(1, 4).add(new Paragraph("TOTAL").setFont(fontBold).setFontSize(10)
                        .setTextAlignment(TextAlignment.RIGHT))
                .setBackgroundColor(new DeviceRgb(30, 64, 175)).setPadding(8).setBorder(null)
                .setFontColor(ColorConstants.WHITE));
        table.addCell(new Cell().add(new Paragraph(formatAmount(total, contract.getCurrency()))
                        .setFont(fontBold).setFontSize(10).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(ACCENT_COLOR).setPadding(8).setBorder(null));

        doc.add(table);
    }

        private void addSignaturesTable(Document doc,
                                                                        PdfFont fontBold,
                                                                        PdfFont fontRegular,
                                                                        Contract contract,
                                                                        String clientName,
                                                                        String freelancerName) {

        Table table = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100));

        // Freelancer
        String freelancerSig = contract.getFreelancerSignedAt() != null
                ? "✅ Signé le " + contract.getFreelancerSignedAt().format(DATETIME_FMT)
                : "⏳ En attente";

        String freelancerLabel = freelancerName != null && !freelancerName.isBlank()
                ? freelancerName
                : "Freelancer #" + contract.getFreelancerId();

        Cell freelancerCell = new Cell()
                .add(new Paragraph("FREELANCER").setFont(fontBold).setFontSize(10).setFontColor(PRIMARY_COLOR))
                .add(new Paragraph(freelancerLabel).setFont(fontRegular).setFontSize(9))
                                .add(new Paragraph("\n"));

                // Image de signature du freelancer (si disponible)
                if (contract.getFreelancerSignatureImagePath() != null) {
                        try {
                                Image img = new Image(ImageDataFactory.create(
                                                java.nio.file.Paths.get(contract.getFreelancerSignatureImagePath()).toAbsolutePath().toString()));
                                img.setAutoScale(true).setMaxHeight(80);
                                freelancerCell.add(img);
                        } catch (Exception e) {
                                System.err.println("⚠️ [PDF] Impossible de charger l'image de signature freelancer : " + e.getMessage());
                        }
                }

                freelancerCell.add(new Paragraph(freelancerSig).setFont(fontBold).setFontSize(9)
                                                .setFontColor(contract.getFreelancerSignedAt() != null ? ACCENT_COLOR : new DeviceRgb(245, 158, 11)))
                                .setPadding(15).setMarginRight(5)
                                .setBorder(new SolidBorder(BORDER_GRAY, 1));

        // Client
        String clientSig = contract.getClientSignedAt() != null
                ? "✅ Signé le " + contract.getClientSignedAt().format(DATETIME_FMT)
                : "⏳ En attente";

        String clientLabel = clientName != null && !clientName.isBlank()
                ? clientName
                : "Client #" + contract.getClientId();

        Cell clientCell = new Cell()
                .add(new Paragraph("CLIENT").setFont(fontBold).setFontSize(10).setFontColor(PRIMARY_COLOR))
                .add(new Paragraph(clientLabel).setFont(fontRegular).setFontSize(9))
                                .add(new Paragraph("\n"));

                // Image de signature du client (si disponible)
                if (contract.getClientSignatureImagePath() != null) {
                        try {
                                Image img = new Image(ImageDataFactory.create(
                                                java.nio.file.Paths.get(contract.getClientSignatureImagePath()).toAbsolutePath().toString()));
                                img.setAutoScale(true).setMaxHeight(80);
                                clientCell.add(img);
                        } catch (Exception e) {
                                System.err.println("⚠️ [PDF] Impossible de charger l'image de signature client : " + e.getMessage());
                        }
                }

                clientCell.add(new Paragraph(clientSig).setFont(fontBold).setFontSize(9)
                                                .setFontColor(contract.getClientSignedAt() != null ? ACCENT_COLOR : new DeviceRgb(245, 158, 11)))
                                .setPadding(15).setMarginLeft(5)
                                .setBorder(new SolidBorder(BORDER_GRAY, 1));

        table.addCell(freelancerCell);
        table.addCell(clientCell);
        doc.add(table);
    }

    private void addFooter(Document doc, PdfFont fontRegular, Contract contract) {
        doc.add(new Paragraph("\n"));
        doc.add(new Paragraph("Ce contrat a été généré automatiquement par la plateforme SmartPlatform · Projet #"
                + contract.getProjectId() + " · Contrat #" + contract.getId())
                .setFont(fontRegular).setFontSize(8)
                .setFontColor(new DeviceRgb(148, 163, 184))
                .setTextAlignment(TextAlignment.CENTER)
                .setBorderTop(new SolidBorder(BORDER_GRAY, 0.5f))
                .setPaddingTop(8));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private Cell cellOf(String text, PdfFont font, float size, DeviceRgb bg) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(size))
                .setBackgroundColor(bg).setPadding(5)
                .setBorder(new SolidBorder(BORDER_GRAY, 0.5f));
    }

    private String formatAmount(BigDecimal amount, String currency) {
        if (amount == null) return "0.00 " + (currency != null ? currency : "TND");
        return String.format("%,.2f %s", amount, currency != null ? currency : "TND");
    }
}