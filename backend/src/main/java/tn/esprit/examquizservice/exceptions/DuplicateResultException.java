package tn.esprit.examquizservice.exceptions;

public class DuplicateResultException extends RuntimeException {
    public DuplicateResultException(String message) {
        super(message);
    }
}
