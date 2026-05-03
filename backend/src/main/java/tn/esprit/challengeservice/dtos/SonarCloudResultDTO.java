package tn.esprit.challengeservice.dtos;

import lombok.Data;

@Data
public class SonarCloudResultDTO {
    private String qualityGateStatus;
    private int bugs;
    private int codeSmells;
    private int vulnerabilities;
    private int securityHotspots;
    private double coverage;
    private double duplication;
    private int linesOfCode;
}