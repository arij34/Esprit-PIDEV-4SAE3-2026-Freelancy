package tn.esprit.examquizservice.config;

/**
 * Configuration constants for the proctoring system.
 */
public class ProctoringConfig {
    
    // Violation thresholds
    public static final int VIOLATION_THRESHOLD_AUTO_SUBMIT = 3;
    public static final int VIOLATION_THRESHOLD_WARNING = 1;
    
    // Violation types and their priorities
    public enum ViolationPriority {
        CRITICAL("PHONE_DETECTED", "EXIT_FULLSCREEN"),      // Immediate termination
        HIGH("MULTIPLE_PEOPLE", "NO_FACE"),                  // Multiple violations trigger auto-submit
        MEDIUM("LOOKING_AWAY", "SUSPICIOUS_MOVEMENT"),       // Warnings
        LOW("TAB_SWITCH");                                   // Informational
        
        private final String[] types;
        
        ViolationPriority(String... types) {
            this.types = types;
        }
        
        public String[] getTypes() {
            return types;
        }
    }
    
    // Time-based rules
    public static final int NO_FACE_WARNING_THRESHOLD_SECONDS = 5;
    public static final int VIOLATION_COOLDOWN_SECONDS = 2;  // Prevent duplicate violations within 2 seconds
    
    // Messaging
    public static final String PHONE_DETECTED_MESSAGE = "Camera detected a phone. Exam will be terminated.";
    public static final String VIOLATION_THRESHOLD_MESSAGE = "Violation limit reached. Your exam will be auto-submitted.";
    public static final String VIOLATION_WARNING_MESSAGE = "Violation recorded. Please follow exam guidelines.";
    
    private ProctoringConfig() {
        // Private constructor to prevent instantiation
    }
}
