package tn.esprit.examquizservice.exceptions;

/**
 * Exception thrown when business rule validation fails.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
